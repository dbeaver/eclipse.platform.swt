package org.eclipse.swt.widgets;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved
 */

import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.win32.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.events.*;

/**
 * Instances of this class are selectable user interface
 * objects that represent a range of positive, numeric values. 
 * <p>
 * At any given moment, a given slider will have a 
 * single <em>selection</em> that is considered to be its
 * value, which is constrained to be within the range of
 * values the slider represents (that is, between its
 * <em>minimum</em> and <em>maximum</em> values).
 * </p><p>
 * Typically, sliders will be made up of five areas:
 * <ol>
 * <li>an arrow button for decrementing the value</li>
 * <li>a page decrement area for decrementing the value by a larger amount</li>
 * <li>a <em>thumb</em> for modifying the value by mouse dragging</li>
 * <li>a page increment area for incrementing the value by a larger amount</li>
 * <li>an arrow button for incrementing the value</li>
 * </ol>
 * Based on their style, sliders are either <code>HORIZONTAL</code>
 * (which have left and right facing buttons for incrementing and
 * decrementing the value) or <code>VERTICAL</code> (which have
 * up and down facing buttons for incrementing and decrementing
 * the value).
 * </p><p>
 * On some platforms, the size of the slider's thumb can be
 * varied relative to the magnitude of the range of values it
 * represents (that is, relative to the difference between its
 * maximum and minimum values). Typically, this is used to
 * indicate some proportional value such as the ratio of the
 * visible area of a document to the total amount of space that
 * it would take to display it. SWT supports setting the thumb
 * size even if the underlying platform does not, but in this
 * case the appearance of the slider will not change.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>HORIZONTAL, VERTICAL</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 *
 * @see ScrollBar
 */
public class Slider extends Control {
	int increment, pageIncrement;
	static final int ScrollBarProc;
	static final TCHAR ScrollBarClass = new TCHAR (0, "SCROLLBAR", true);
	static {
		WNDCLASS lpWndClass = new WNDCLASS ();
		OS.GetClassInfo (0, ScrollBarClass, lpWndClass);
		ScrollBarProc = lpWndClass.lpfnWndProc;
	}

/**
 * Constructs a new instance of this class given its parent
 * and a style value describing its behavior and appearance.
 * <p>
 * The style value is either one of the style constants defined in
 * class <code>SWT</code> which is applicable to instances of this
 * class, or must be built by <em>bitwise OR</em>'ing together 
 * (that is, using the <code>int</code> "|" operator) two or more
 * of those <code>SWT</code> style constants. The class description
 * for all SWT widget classes should include a comment which
 * describes the style constants which are applicable to the class.
 * </p>
 *
 * @param parent a composite control which will be the parent of the new instance (cannot be null)
 * @param style the style of control to construct
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
 *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
 * </ul>
 *
 * @see SWT
 * @see Widget#checkSubclass
 * @see Widget#getStyle
 */
public Slider (Composite parent, int style) {
	super (parent, checkStyle (style));
}

/**
 * Adds the listener to the collection of listeners who will
 * be notified when the receiver's value changes, by sending
 * it one of the messages defined in the <code>SelectionListener</code>
 * interface.
 * <p>
 * When <code>widgetSelected</code> is called, the event object detail field contains one of the following values:
 * <code>0</code> - for the end of a drag.
 * <code>SWT.DRAG</code>.
 * <code>SWT.HOME</code>.
 * <code>SWT.END</code>.
 * <code>SWT.ARROW_DOWN</code>.
 * <code>SWT.ARROW_UP</code>.
 * <code>SWT.PAGE_DOWN</code>.
 * <code>SWT.PAGE_UP</code>.
 * <code>widgetDefaultSelected</code> is not called.
 * </p>
 *
 * @param listener the listener which should be notified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see SelectionListener
 * @see #removeSelectionListener
 * @see SelectionEvent
 */
public void addSelectionListener (SelectionListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	TypedListener typedListener = new TypedListener(listener);
	addListener (SWT.Selection,typedListener);
	addListener (SWT.DefaultSelection,typedListener);
}

int callWindowProc (int msg, int wParam, int lParam) {
	if (handle == 0) return 0;
	return OS.CallWindowProc (ScrollBarProc, handle, msg, wParam, lParam);
}

static int checkStyle (int style) {
	return checkBits (style, SWT.HORIZONTAL, SWT.VERTICAL, 0, 0, 0, 0);
}

public Point computeSize (int wHint, int hHint, boolean changed) {
	checkWidget ();
	int border = getBorderWidth ();
	int width = border * 2, height = border * 2;
	if ((style & SWT.HORIZONTAL) != 0) {
		width += OS.GetSystemMetrics (OS.SM_CXHSCROLL) * 10;
		height += OS.GetSystemMetrics (OS.SM_CYHSCROLL);
	} else {
		width += OS.GetSystemMetrics (OS.SM_CXVSCROLL);
		height += OS.GetSystemMetrics (OS.SM_CYVSCROLL) * 10;
	}
	if (wHint != SWT.DEFAULT) width = wHint + (border * 2);
	if (hHint != SWT.DEFAULT) height = hHint + (border * 2);
	return new Point (width, height);
}

void createWidget () {
	super.createWidget ();
	increment = 1;
	pageIncrement = 10;
	/*
	* Set the intial values of the maximum
	* to 100 and the thumb to 10.  Note that
	* info.nPage needs to be 11 in order to
	* get a thumb that is 10.
	*/
	SCROLLINFO info = new SCROLLINFO ();
	info.cbSize = SCROLLINFO.sizeof;
	info.fMask = OS.SIF_ALL;
	info.nMax = 100;
	info.nPage = 11;
	OS.SetScrollInfo (handle, OS.SB_CTL, info, true);
}

int defaultBackground () {
	return OS.GetSysColor (OS.COLOR_SCROLLBAR);
}

int defaultForeground () {
	return OS.GetSysColor (OS.COLOR_BTNFACE);
}

public boolean getEnabled () {
	checkWidget ();
	return (state & DISABLED) == 0;
}

/**
 * Returns the amount that the receiver's value will be
 * modified by when the up/down (or right/left) arrows
 * are pressed.
 *
 * @return the increment
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getIncrement () {
	checkWidget ();
	return increment;
}

/**
 * Returns the maximum value which the receiver will allow.
 *
 * @return the maximum
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getMaximum () {
	checkWidget ();
	SCROLLINFO info = new SCROLLINFO ();
	info.cbSize = SCROLLINFO.sizeof;
	info.fMask = OS.SIF_RANGE;
	OS.GetScrollInfo (handle, OS.SB_CTL, info);
	return info.nMax;
}

/**
 * Returns the minimum value which the receiver will allow.
 *
 * @return the minimum
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getMinimum () {
	checkWidget ();
	SCROLLINFO info = new SCROLLINFO ();
	info.cbSize = SCROLLINFO.sizeof;
	info.fMask = OS.SIF_RANGE;
	OS.GetScrollInfo (handle, OS.SB_CTL, info);
	return info.nMin;
}

/**
 * Returns the amount that the receiver's value will be
 * modified by when the page increment/decrement areas
 * are selected.
 *
 * @return the page increment
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getPageIncrement () {
	checkWidget ();
	return pageIncrement;
}

/**
 * Returns the single <em>selection</em> that is the receiver's value.
 *
 * @return the selection
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getSelection () {
	checkWidget ();
	SCROLLINFO info = new SCROLLINFO ();
	info.cbSize = SCROLLINFO.sizeof;
	info.fMask = OS.SIF_POS;
	OS.GetScrollInfo (handle, OS.SB_CTL, info);
	return info.nPos;
}

/**
 * Returns the size of the receiver's thumb relative to the
 * difference between its maximum and minimum values.
 *
 * @return the thumb value
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getThumb () {
	checkWidget ();
	SCROLLINFO info = new SCROLLINFO ();
	info.cbSize = SCROLLINFO.sizeof;
	info.fMask = OS.SIF_PAGE;
	OS.GetScrollInfo (handle, OS.SB_CTL, info);
	if (info.nPage != 0) --info.nPage;
	return info.nPage;
}

/**
 * Removes the listener from the collection of listeners who will
 * be notified when the receiver's value changes.
 *
 * @param listener the listener which should no longer be notified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see SelectionListener
 * @see #addSelectionListener
 */
public void removeSelectionListener (SelectionListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook (SWT.Selection, listener);
	eventTable.unhook (SWT.DefaultSelection,listener);	
}

void setBounds (int x, int y, int width, int height, int flags) {
	super.setBounds (x, y, width, height, flags);
	/*
	* Bug in Windows.  If the scroll bar is resized when it has focus,
	* the flashing cursor that is used to show that the scroll bar has
	* focus is not moved.  The fix is to post a fake WM_SETFOCUS to
	* get the scroll bar to recompute the size of the flashing cursor.
	*/
	if (OS.GetFocus () == handle) {
		OS.PostMessage (handle, OS.WM_SETFOCUS, 0, 0);
	}
}

public void setEnabled (boolean enabled) {
	checkWidget ();
	int flags = OS.ESB_DISABLE_BOTH;
	if (enabled) flags = OS.ESB_ENABLE_BOTH;
	if (OS.IsWinCE) {
		super.setEnabled (enabled);
	} else {
		OS.EnableScrollBar (handle, OS.SB_CTL, flags);
	}
	state &= ~DISABLED;
	if (!enabled) state |= DISABLED;
}

/**
 * Sets the amount that the receiver's value will be
 * modified by when the up/down (or right/left) arrows
 * are pressed to the argument, which must be at least 
 * one.
 *
 * @param value the new increment (must be greater than zero)
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setIncrement (int value) {
	checkWidget ();
	if (value < 1) return;
	increment = value;
}

/**
 * Sets the maximum value which the receiver will allow
 * to be the argument which must be greater than or
 * equal to zero.
 *
 * @param value the new maximum (must be zero or greater)
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setMaximum (int value) {
	checkWidget ();
	if (value < 0) return;
	SCROLLINFO info = new SCROLLINFO ();
	info.cbSize = SCROLLINFO.sizeof;
	info.fMask = OS.SIF_RANGE | OS.SIF_DISABLENOSCROLL;
	OS.GetScrollInfo (handle, OS.SB_CTL, info);
	if (value - info.nMin - info.nPage < 1) return;
	info.nMax = value;
	OS.SetScrollInfo (handle, OS.SB_CTL, info, (state & DISABLED) == 0);
		
	/*
	* Feature in Windows.  Using SIF_DISABLENOSCROLL,
	* SetScrollInfo () can change enabled and disabled
	* state of the scroll bar causing a scroll bar that
	* was disabled by the application to become enabled.
	* The fix is to disable the scroll bar (again) when
	* the application has disabled the scroll bar.
	*/
	if ((state & DISABLED) != 0) {
		if (OS.IsWinCE) {
			OS.EnableWindow (handle, false);
		} else {
			OS.EnableScrollBar (handle, OS.SB_CTL, OS.ESB_DISABLE_BOTH);
		}
	}
	
	/*
	* Bug in Windows.  If the thumb is resized when it has focus,
	* the flashing cursor that is used to show that the scroll bar has
	* focus is not moved.  The fix is to post a fake WM_SETFOCUS to
	* get the scroll bar to recompute the size of the flashing cursor.
	*/
	if (OS.GetFocus () == handle) {
		OS.PostMessage (handle, OS.WM_SETFOCUS, 0, 0);
	}
}

/**
 * Sets the minimum value which the receiver will allow
 * to be the argument which must be greater than or
 * equal to zero.
 *
 * @param value the new minimum (must be zero or greater)
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setMinimum (int value) {
	checkWidget ();
	if (value < 0) return;
	SCROLLINFO info = new SCROLLINFO ();
	info.cbSize = SCROLLINFO.sizeof;
	info.fMask = OS.SIF_RANGE | OS.SIF_DISABLENOSCROLL;
	OS.GetScrollInfo (handle, OS.SB_CTL, info);
	if (info.nMax - value - info.nPage < 1) return;
	info.nMin = value;
	OS.SetScrollInfo (handle, OS.SB_CTL, info, true);

	/*
	* Feature in Windows.  Using SIF_DISABLENOSCROLL,
	* SetScrollInfo () can change enabled and disabled
	* state of the scroll bar causing a scroll bar that
	* was disabled by the application to become enabled.
	* The fix is to disable the scroll bar (again) when
	* the application has disabled the scroll bar.
	*/
	if ((state & DISABLED) != 0) {
		if (OS.IsWinCE) {
			OS.EnableWindow (handle, false);
		} else {
			OS.EnableScrollBar (handle, OS.SB_CTL, OS.ESB_DISABLE_BOTH);
		}
	}
	
	/*
	* Bug in Windows.  If the thumb is resized when it has focus,
	* the flashing cursor that is used to show that the scroll bar has
	* focus is not moved.  The fix is to post a fake WM_SETFOCUS to
	* get the scroll bar to recompute the size of the flashing cursor.
	*/
	if (OS.GetFocus () == handle) {
		OS.PostMessage (handle, OS.WM_SETFOCUS, 0, 0);
	}
}

/**
 * Sets the amount that the receiver's value will be
 * modified by when the page increment/decrement areas
 * are selected to the argument, which must be at least
 * one.
 *
 * @return the page increment (must be greater than zero)
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setPageIncrement (int value) {
	checkWidget ();
	if (value < 1) return;
	pageIncrement = value;
}

/**
 * Sets the single <em>selection</em> that is the receiver's
 * value to the argument which must be greater than or equal
 * to zero.
 *
 * @param value the new selection (must be zero or greater)
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setSelection (int value) {
	checkWidget ();
	if (value < 0) return;
	SCROLLINFO info = new SCROLLINFO ();
	info.cbSize = SCROLLINFO.sizeof;
	info.fMask = OS.SIF_POS;
	info.nPos = value;
	OS.SetScrollInfo (handle, OS.SB_CTL, info, true);
}

/**
 * Sets the size of the receiver's thumb relative to the
 * difference between its maximum and minimum values to the
 * argument which must be at least one.
 *
 * @param value the new thumb value (must be at least one)
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see ScrollBar
 */
public void setThumb (int value) {
	checkWidget ();

	/* Position the thumb */
	if (value < 1) return;
	SCROLLINFO info = new SCROLLINFO ();
	info.cbSize = SCROLLINFO.sizeof;
	info.fMask = OS.SIF_PAGE | OS.SIF_RANGE | OS.SIF_DISABLENOSCROLL;
	OS.GetScrollInfo (handle, OS.SB_CTL, info);
	if (info.nMax - info.nMin - value < 0) return;
	info.nPage = value;
	if (info.nPage != 0) info.nPage++;
	OS.SetScrollInfo (handle, OS.SB_CTL, info, true);
		
	/*
	* Feature in Windows.  Using SIF_DISABLENOSCROLL,
	* SetScrollInfo () can change enabled and disabled
	* state of the scroll bar causing a scroll bar that
	* was disabled by the application to become enabled.
	* The fix is to disable the scroll bar (again) when
	* the application has disabled the scroll bar.
	*/
	if ((state & DISABLED) != 0) {
		if (OS.IsWinCE) {
			OS.EnableWindow (handle, false);
		} else {
			OS.EnableScrollBar (handle, OS.SB_CTL, OS.ESB_DISABLE_BOTH);
		}
	}
	
	/*
	* Bug in Windows.  If the thumb is resized when it has focus,
	* the flashing cursor that is used to show that the scroll bar has
	* focus is not moved.  The fix is to post a fake WM_SETFOCUS to
	* get the scroll bar to recompute the size of the flashing cursor.
	*/
	if (OS.GetFocus () == handle) {
		OS.PostMessage (handle, OS.WM_SETFOCUS, 0, 0);
	}
}

/**
 * Sets the receiver's selection, minimum value, maximum
 * value, thumb, increment and page increment all at once.
 * <p>
 * Note: This is equivalent to setting the values individually
 * using the appropriate methods, but may be implemented in a 
 * more efficient fashion on some platforms.
 * </p>
 *
 * @param selection the new selection value
 * @param minimum the new minimum value
 * @param maximum the new maximum value
 * @param thumb the new thumb value
 * @param increment the new increment value
 * @param pageIncrement the new pageIncrement value
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setValues (int selection, int minimum, int maximum, int thumb, int increment, int pageIncrement) {
	checkWidget ();
	if (selection < 0) return;
	if (minimum < 0) return;
	if (maximum < 0) return;
	if (thumb < 1) return;
	if (maximum - minimum - thumb < 0) return;
	if (increment < 1) return;
	if (pageIncrement < 1) return;
	this.increment = increment;
	this.pageIncrement = pageIncrement;
	SCROLLINFO info = new SCROLLINFO ();
	info.cbSize = SCROLLINFO.sizeof;
	info.fMask = OS.SIF_POS | OS.SIF_PAGE | OS.SIF_RANGE | OS.SIF_DISABLENOSCROLL;
	info.nPos = selection;
	info.nMin = minimum;
	info.nMax = maximum;
	info.nPage = thumb;
	if (info.nPage != 0) info.nPage++;
	OS.SetScrollInfo (handle, OS.SB_CTL, info, true);
		
	/*
	* Feature in Windows.  Using SIF_DISABLENOSCROLL,
	* SetScrollInfo () can change enabled and disabled
	* state of the scroll bar causing a scroll bar that
	* was disabled by the application to become enabled.
	* The fix is to disable the scroll bar (again) when
	* the application has disabled the scroll bar.
	*/
	if ((state & DISABLED) != 0) {
		if (OS.IsWinCE) {
			OS.EnableWindow (handle, false);
		} else {
			OS.EnableScrollBar (handle, OS.SB_CTL, OS.ESB_DISABLE_BOTH);
		}
	}
	
	/*
	* Bug in Windows.  If the thumb is resized when it has focus,
	* the flashing cursor that is used to show that the scroll bar has
	* focus is not moved.  The fix is to post a fake WM_SETFOCUS to
	* get the scroll bar to recompute the size of the flashing cursor.
	*/
	if (OS.GetFocus () == handle) {
		OS.PostMessage (handle, OS.WM_SETFOCUS, 0, 0);
	}
}

int widgetExtStyle () {
	/*
	* Bug in Windows.  If a scroll bar control is given a border,
	* dragging the scroll bar thumb eats away parts of the border 
	* while the thumb is dragged.  The fix is to clear WS_BORDER
	* for all scroll bars.
	*/
	int bits = super.widgetExtStyle ();
	if ((style & SWT.BORDER) != 0) bits &= ~OS.WS_EX_CLIENTEDGE;
	return bits;
}

int widgetStyle () {
	int bits = super.widgetStyle () | OS.WS_TABSTOP;
	/*
	* Bug in Windows.  If a scroll bar control is given a border,
	* dragging the scroll bar thumb eats away parts of the border 
	* while the thumb is dragged.  The fix is to clear WS_BORDER.
	*/
	if ((style & SWT.BORDER) != 0) bits &= ~OS.WS_BORDER;
	if ((style & SWT.HORIZONTAL) != 0) return bits | OS.SBS_HORZ;
	return bits | OS.SBS_VERT;
}

TCHAR windowClass () {
	return ScrollBarClass;
}

int windowProc () {
	return ScrollBarProc;
}

LRESULT WM_LBUTTONDBLCLK (int wParam, int lParam) {
	
	/*
	* Feature in Windows.  For some reason, capturing
	* the mouse after processing WM_LBUTTONDBLCLK for the
	* widget interferes with the normal mouse processing
	* for the widget.  The fix is to avoid the automatic
	* mouse capture.
	*/

	/*
	* Feature in Windows.  Windows uses the WS_TABSTOP
	* style for the scroll bar to decide that focus
	* should be set during WM_LBUTTONDBLCLK.  This is
	* not the desired behavior.  The fix is to clear
	* and restore WS_TABSTOP so that Windows will not
	* assign focus.
	*/

	int hwndCapture = OS.GetCapture ();
	int oldBits = OS.GetWindowLong (handle, OS.GWL_STYLE);
	int newBits = oldBits & ~OS.WS_TABSTOP;
	OS.SetWindowLong (handle, OS.GWL_STYLE, newBits);	
	LRESULT result = super.WM_LBUTTONDBLCLK (wParam, lParam);
	OS.SetWindowLong (handle, OS.GWL_STYLE, oldBits);
	if (OS.GetCapture () != hwndCapture) OS.SetCapture (hwndCapture);
	return result;
}

LRESULT WM_LBUTTONDOWN (int wParam, int lParam) {
	
	/*
	* Feature in Windows.  For some reason, capturing
	* the mouse after processing WM_LBUTTONDOWN for the
	* widget interferes with the normal mouse processing
	* for the widget.  The fix is to avoid the automatic
	* mouse capture.
	*/

	/*
	* Feature in Windows.  Windows uses the WS_TABSTOP
	* style for the scroll bar to decide that focus
	* should be set during WM_LBUTTONDOWN.  This is
	* not the desired behavior.  The fix is to clear
	* and restore WS_TABSTOP so that Windows will not
	* assign focus.
	*/

	int hwndCapture = OS.GetCapture ();
	int oldBits = OS.GetWindowLong (handle, OS.GWL_STYLE);
	int newBits = oldBits & ~OS.WS_TABSTOP;
	OS.SetWindowLong (handle, OS.GWL_STYLE, newBits);	
	LRESULT result = super.WM_LBUTTONDOWN (wParam, lParam);
	OS.SetWindowLong (handle, OS.GWL_STYLE, oldBits);
	if (OS.GetCapture () != hwndCapture) OS.SetCapture (hwndCapture);
	return result;
}

LRESULT wmScrollChild (int wParam, int lParam) {

	/* Do nothing when scrolling is ending */
	int code = wParam & 0xFFFF;
	if (code == OS.SB_ENDSCROLL) return null;

	/* Move the thumb */
	Event event = new Event ();
	SCROLLINFO info = new SCROLLINFO ();
	info.cbSize = SCROLLINFO.sizeof;
	info.fMask = OS.SIF_TRACKPOS | OS.SIF_POS | OS.SIF_RANGE;
	OS.GetScrollInfo (handle, OS.SB_CTL, info);
	info.fMask = OS.SIF_POS;
	switch (code) {
		case OS.SB_THUMBPOSITION:
			/*
			* Do not set the detail field to DRAG to
			* indicate that the dragging has ended.
			*/
			info.nPos = info.nTrackPos;
			break;
		case OS.SB_THUMBTRACK:
			event.detail = SWT.DRAG;
			info.nPos = info.nTrackPos;
			break;
		case OS.SB_TOP:
			event.detail = SWT.HOME;
			info.nPos = info.nMin;
			break;
		case OS.SB_BOTTOM:
			event.detail = SWT.END;
			info.nPos = info.nMax;
			break;
		case OS.SB_LINEDOWN:
			event.detail = SWT.ARROW_DOWN;
			info.nPos += increment;
			break;
		case OS.SB_LINEUP:
			event.detail = SWT.ARROW_UP;
			info.nPos = Math.max (info.nMin, info.nPos - increment);
			break;
		case OS.SB_PAGEDOWN:
			event.detail = SWT.PAGE_DOWN;
			info.nPos += pageIncrement;
			break;
		case OS.SB_PAGEUP:
			event.detail = SWT.PAGE_UP;
			info.nPos = Math.max (info.nMin, info.nPos - pageIncrement);
			break;
	}
	OS.SetScrollInfo (handle, OS.SB_CTL, info, true);
	
	/*
	* Send the event because WM_HSCROLL and
	* WM_VSCROLL are sent from a modal message
	* loop in Windows that is active when the
	* user is scrolling.
	*/
	sendEvent (SWT.Selection, event);
	// the widget could be destroyed at this point
	return null;
}

}
