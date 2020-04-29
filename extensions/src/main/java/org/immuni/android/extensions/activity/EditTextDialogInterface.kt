package org.immuni.android.extensions.activity

import android.content.DialogInterface

interface EditTextDialogInterface {

    /**
     * Interface used to allow the creator of a dialog to run some code when an
     * item on the dialog is clicked.
     */
    interface OnClickListener {
        /**
         * This method will be invoked when a button in the dialog is clicked.
         *
         * @param dialog the dialog that received the click
         * @param which the button that was clicked (ex.
         * [DialogInterface.BUTTON_POSITIVE]) or the position
         * of the item clicked
         */
        fun onClick(dialog: DialogInterface, which: Int, text: String)
    }
}