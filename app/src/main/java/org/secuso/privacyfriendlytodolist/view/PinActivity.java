package org.secuso.privacyfriendlytodolist.view;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import org.secuso.privacyfriendlytodolist.view.dialog.PinDialog;

public class PinActivity extends AppCompatActivity {
    public static Boolean result = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // reset result and start dialog
        result = null;
        startDialog();
    }

    private void startDialog() {
        PinDialog pinDialog = new PinDialog(this);
        pinDialog.setCallback(new PinDialog.PinCallback() {
            @Override
            public void accepted() {
                result = true;
                PinActivity.this.finish();
            }

            @Override
            public void declined() {
                result = false;
                PinActivity.this.finish();
            }

            @Override
            public void resetApp() {
                result = false;
                PinActivity.this.finish();
            }
        });
        pinDialog.setDisallowReset(true);
        pinDialog.setOnCancelListener(dialog -> {
        result = false;
        PinActivity.this.finish();
    });
        pinDialog.setOnDismissListener(dialog -> {
        result = false;
        PinActivity.this.finish();
    });
        pinDialog.show();
    }
}
