/*******************************************************************************
 * Copyright (c) 2005, 2010 Stein K. Skytteren and Christian Schwarz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Stein K. Skytteren and Christian Schwarz - initial API and implementation
 *******************************************************************************/
package org.cubictest.ui.gef.command;

import static org.cubictest.model.ActionType.CLICK;
import static org.cubictest.model.IdentifierType.ALT;
import static org.cubictest.model.IdentifierType.LABEL;

import java.util.List;

import org.cubictest.common.utils.ErrorHandler;
import org.cubictest.common.utils.ModelUtil;
import org.cubictest.common.utils.UserInfo;
import org.cubictest.model.Common;
import org.cubictest.model.CommonTransition;
import org.cubictest.model.ConnectionPoint;
import org.cubictest.model.CustomTestStepHolder;
import org.cubictest.model.ExtensionPoint;
import org.cubictest.model.ExtensionTransition;
import org.cubictest.model.IActionElement;
import org.cubictest.model.Image;
import org.cubictest.model.Link;
import org.cubictest.model.Page;
import org.cubictest.model.PageElement;
import org.cubictest.model.SimpleTransition;
import org.cubictest.model.SubTest;
import org.cubictest.model.Test;
import org.cubictest.model.Transition;
import org.cubictest.model.TransitionNode;
import org.cubictest.model.UserInteraction;
import org.cubictest.model.UserInteractionsTransition;
import org.cubictest.ui.gef.controller.PageEditPart;
import org.cubictest.ui.gef.interfaces.exported.ITestEditor;
import org.cubictest.ui.gef.wizards.ExposeExtensionPointWizard;
import org.cubictest.ui.gef.wizards.NewUserInteractionsWizard;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.DirectEditRequest;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * A command that creates a <code>Transition</code>.
 * 
 * @author SK Skytteren
 * @author chr_schwarz
 */
public class CreateTransitionCommand extends Command {

	private TransitionNode sourceNode;

	private TransitionNode targetNode;

	/** Transition cached for redo, or initial transition to add */
	private Transition transition;

	private Test test;
	
	private PageEditPart pageEditPart;
	
	private boolean autoCreateTargetPage = false;
	
	private boolean skipLegalTransitionCheck;
	
	boolean executed = false;

	private PageElement selectedPageElement;

	/**
	 * Constructor for case when transition is pre-created with source and target set.
	 * @param test
	 * @param transition
	 */
	public CreateTransitionCommand(Transition transition, Test test, boolean skipLegalTransitionCheck) {
		this.transition = transition;
		this.skipLegalTransitionCheck = skipLegalTransitionCheck;
		this.sourceNode = transition.getStart();
		this.targetNode = transition.getEnd();
		this.test = test;
	}

	public CreateTransitionCommand() {
	}

	/*
	 * @see org.eclipse.gef.commands.Command#canExecute()
	 */
	@Override
	public boolean canExecute() {
		if (transition != null || skipLegalTransitionCheck) {
			return true;
		}
		if (autoCreateTargetPage) {
			return true;
		}
		return (ModelUtil.isLegalTransition(sourceNode, targetNode, false, false) == ModelUtil.TRANSITION_EDIT_VALID);
	}


	/*
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	@Override
	public void execute() {
		super.execute();
		if (executed) {
			//idempotent
			return;
		}
		if (autoCreateTargetPage) {
			targetNode = new Page();
			targetNode.setName("New page");
			Point position = sourceNode.getPosition().getCopy();
			if (pageEditPart == null) {
				ErrorHandler.logAndThrow("PageEditPart not set. Cannot auto create target page.");
			}
			position.y = position.y + this.pageEditPart.getContentPane().getClientArea().height + 90;
			int outTransitions = 0;
			if (sourceNode.getOutTransitions() != null) {
				for (Transition transition : sourceNode.getOutTransitions()) {
					if (!(transition instanceof SimpleTransition)) {
						outTransitions++;
					}
				}				
				position.x = position.x + (outTransitions * ITestEditor.NEW_PATH_OFFSET);
			}
			targetNode.setPosition(position);
			test.addPage((Page) targetNode);
		}
		
		if (transition != null) {
			//pre-populated transition, add directly:
			test.addTransition(transition);
		}
		else {
			//transition not pre-populated: 
			
			if (sourceNode instanceof SubTest && (targetNode instanceof Page
					|| targetNode instanceof SubTest || targetNode instanceof CustomTestStepHolder)) {
				//ExtensionTransition from SubTest
				SubTest subTest = (SubTest) sourceNode;
				List<ExtensionPoint> exPoints = subTest.getTest(true).getAllExtensionPoints();
				if (exPoints == null || exPoints.size() == 0) {
					if(!ModelUtil.hasOnlyOnePathFromNodeToEndOfTest(test.getStartPoint())) {
						UserInfo.showErrorDialog("The \"" + subTest.getFileName() + "\" subtest has more than one path and " +
								"does not contain any extension points.\n" +
						"To continue, add an extension point to the subtest or remove the excessive paths and then retry this operation.");
						return;
						
					}
					transition = new SimpleTransition(sourceNode, targetNode);
					test.addTransition(transition);
				}
				else if (exPoints.size() == 1) {
					//auto select the single exPoint 
					transition = new ExtensionTransition(sourceNode, targetNode, exPoints.get(0));
					test.addTransition(transition);
				}
				else {
					//open dialog to select which exPoint to extend from:
					ExposeExtensionPointWizard exposeExtensionPointWizard = new ExposeExtensionPointWizard(
							(SubTest) sourceNode, test);
					WizardDialog dlg = new WizardDialog(new Shell(),
							exposeExtensionPointWizard);
					if (dlg.open() == WizardDialog.CANCEL) {
						return;
					}
					transition = new ExtensionTransition(sourceNode, targetNode,
							exposeExtensionPointWizard.getSelectedExtensionPoint());
					test.addTransition(transition);
				}
			}
			else if(sourceNode instanceof Page && (targetNode instanceof Page || 
					targetNode instanceof SubTest || targetNode instanceof CustomTestStepHolder)) {
				//User Interactions transition:
				transition = new UserInteractionsTransition(sourceNode, targetNode);
				test.addTransition(transition);
				if (sourceNode instanceof Page && ((Page) sourceNode).getRootElements().size() > 0) {
					NewUserInteractionsWizard userActionWizard = new NewUserInteractionsWizard(
							(UserInteractionsTransition) transition, test, selectedPageElement);
					WizardDialog dlg = new WizardDialog(new Shell(), userActionWizard);
	
					if (dlg.open() == WizardDialog.CANCEL) {
						DeleteTransitionCommand cmd = new DeleteTransitionCommand(
								transition, test);
						cmd.execute();
						transition.resetStatus();
						if (autoCreateTargetPage) {
							test.removePage((Page) targetNode);
						}
						return;
					}
				}
				if (autoCreateTargetPage) {
					//if link click: set target page name to name of link 
					List<UserInteraction> actions = ((UserInteractionsTransition) transition).getUserInteractions();
					int last = actions.size() - 1;
					if (last < 0) last = 0;
					IActionElement element = actions.get(last).getElement();
					if (element instanceof Link && actions.get(last).getActionType().equals(CLICK)) {
						targetNode.setName(((Link) element).getIdentifier(LABEL).getValue());
					}
					else if (element instanceof Image && actions.get(last).getActionType().equals(CLICK)) {
						Image image = (Image) element;
						if (image.getIdentifier(ALT).getProbability() > 0) {
							targetNode.setName(image.getIdentifier(ALT).getValue());
						}
					}
					
					//start direct edit:
					for(Object obj : pageEditPart.getParent().getChildren()){
						if (obj instanceof EditPart) {
							EditPart ep = (EditPart) obj;
							if(ep.getModel().equals(targetNode)){
								//Start direct edit: 
								ep.performRequest(new DirectEditRequest());
								break;
							}
						}
					}
				}
				
			}
			else if (sourceNode instanceof Common && targetNode instanceof Page) {
				transition = new CommonTransition((Common) sourceNode,
						(Page) targetNode);
				test.addTransition(transition);
			}
			else if (sourceNode instanceof ConnectionPoint) {
				transition = new SimpleTransition((ConnectionPoint) sourceNode,
						targetNode);
				test.addTransition(transition);
			}
			else if (targetNode instanceof ExtensionPoint) {
				transition = new SimpleTransition(sourceNode, targetNode);
				test.addTransition(transition);
			}		
			
			executed = true;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	@Override
	public void undo() {
		super.undo();
		test.removeTransition(transition);
		if (autoCreateTargetPage) {
			test.removePage((Page) targetNode);
		}
		executed = false;
	}

	@Override
	public void redo() {
		if(transition != null) {
			test.addTransition(transition);
		}
		if (autoCreateTargetPage) {
			test.addPage((Page) targetNode);
		}
		executed = true;
	}


	public void setTest(Test test) {
		this.test = test;
	}

	public void setPageEditPart(PageEditPart pageEditPart) {
		this.pageEditPart = pageEditPart;
	}

	public void setSource(TransitionNode sourceNode) {
		this.sourceNode = sourceNode;
	}

	public void setTarget(TransitionNode targetNode) {
		this.targetNode = targetNode;
	}


	public void setAutoCreateTargetPage(boolean autoCreateTargetPage) {
		this.autoCreateTargetPage = autoCreateTargetPage;
	}


	public boolean isAutoCreateTargetPage() {
		return autoCreateTargetPage;
	}


	public TransitionNode getTarget() {
		return targetNode;
	}


	/** 
	 * Set initial transition to add (do not display input wizard)
	 */
	public void setTransition(Transition transition) {
		this.transition = transition;
	}

	public void setSkipLegalTransitionCheck(boolean skipLegalTransitionCheck) {
		this.skipLegalTransitionCheck = skipLegalTransitionCheck;
	}

	public void setSelectedPageElement(PageElement selectedPageElement) {
		this.selectedPageElement = selectedPageElement;
	}
}