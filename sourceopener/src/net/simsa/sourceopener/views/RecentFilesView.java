package net.simsa.sourceopener.views;

import java.io.IOException;

import net.simsa.sourceopener.Activator;
import net.simsa.sourceopener.IOpenEventListener;
import net.simsa.sourceopener.OpenEvent;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class RecentFilesView extends ViewPart implements IOpenEventListener {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "net.simsa.sourceopener.views.RecentFilesView";

	private TableViewer viewer;
	private Action startSocketServer;
	private Action stopSocketServer;
	private Action doubleClickAction;

	/**
	 * Notify the viewer that the data in the model has changed and it should
	 * update.
	 * This must occur in the UI thread hence the use of syncExec.
	 */
	@Override
	public void onOpenEvent(OpenEvent event)
	{
		Display.getDefault().syncExec(new Runnable() {
			public void run()
			{
				viewer.refresh(false);
			}
		});
	}

	/*
	 * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content
	 * (like Task List, for example).
	 */
	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput)
		{
		}

		public void dispose()
		{
		}

		public Object[] getElements(Object parent)
		{
			return Activator.getDefault().getHttpService().getEventCache().toArray();
		}
	}

	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index)
		{
			return getText(obj);
		}

		public Image getColumnImage(Object obj, int index)
		{
			return getImage(obj);
		}

		public Image getImage(Object obj)
		{
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}

	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public RecentFilesView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent)
	{
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(getViewSite());

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "net.simsa.sourceopener.viewer");
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		registerListener();
	}

	private void registerListener()
	{
		Activator.getDefault().getHttpService().registerListener(this);
	}

	private void hookContextMenu()
	{
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager)
			{
				RecentFilesView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars()
	{
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager)
	{
		manager.add(startSocketServer);
		manager.add(new Separator());
		manager.add(stopSocketServer);
	}

	private void fillContextMenu(IMenuManager manager)
	{
		manager.add(startSocketServer);
		manager.add(stopSocketServer);
		// Other plug-ins can contribute their actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager)
	{
		manager.add(startSocketServer);
		manager.add(stopSocketServer);
	}

	private void makeActions()
	{
		final ImageDescriptor IMAGE_START_ENABLED = Activator.getImageDescriptor("icons/greenplay.gif");
		final ImageDescriptor IMAGE_START_DISABLED = Activator.getImageDescriptor("icons/greenplay_disabled.gif");
		final ImageDescriptor IMAGE_STOP_ENABLED = PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_ELCL_STOP);
		final ImageDescriptor IMAGE_STOP_DISABLED = PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_ELCL_STOP_DISABLED);

		startSocketServer = new Action() {
			public void run()
			{
				try {
					Activator.getDefault().getHttpService().start();
					this.setEnabled(false);
					stopSocketServer.setEnabled(true);
				} catch (IOException io) {
					showMessage("Start listener: Exception while starting: " + io.toString());
				}
			}
		};
		startSocketServer.setText("Start Listener");
		startSocketServer.setToolTipText("Starts the listener to receive browser file-open clicks");
		startSocketServer.setImageDescriptor(IMAGE_START_ENABLED);
		startSocketServer.setDisabledImageDescriptor(IMAGE_START_DISABLED);

		stopSocketServer = new Action() {
			public void run()
			{
				Activator.getDefault().getHttpService().stop();
				this.setEnabled(false);
				startSocketServer.setEnabled(true);
			}
		};
		stopSocketServer.setText("Stop Listener");
		stopSocketServer.setToolTipText("Stops the listener that receives browser file-open clicks");
		stopSocketServer.setImageDescriptor(IMAGE_STOP_ENABLED);
		stopSocketServer.setDisabledImageDescriptor(IMAGE_STOP_DISABLED);
		stopSocketServer.setEnabled(false);

		doubleClickAction = new Action() {
			public void run()
			{
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				showMessage("Double-click detected on " + obj.toString());
			}
		};
	}

	private void hookDoubleClickAction()
	{
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event)
			{
				doubleClickAction.run();
			}
		});
	}

	private void showMessage(String message)
	{
		MessageDialog.openInformation(viewer.getControl().getShell(), "Source Opener Recent Files", message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus()
	{
		viewer.getControl().setFocus();
	}
}