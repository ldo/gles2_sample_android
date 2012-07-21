package nz.gen.geek_central.gles2_sample;
/*
    Direct onscreen display of sample animation.

    Copyright 2012 by Lawrence D'Oliveiro <ldo@geek-central.gen.nz>.

    Licensed under the Apache License, Version 2.0 (the "License"); you may not
    use this file except in compliance with the License. You may obtain a copy of
    the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
    License for the specific language governing permissions and limitations under
    the License.
*/

public class OnScreen extends android.app.Activity
  {
    OnScreenView TheOnScreenView;

    class RenderingDialog
        extends android.app.Dialog
        implements android.content.DialogInterface.OnDismissListener
      {
        final android.content.Context ctx;
        android.widget.RadioGroup TheButtons;

        public RenderingDialog
          (
            android.content.Context ctx
          )
          {
            super(ctx);
            this.ctx = ctx;
          } /*RenderingDialog*/

        @Override
        public void onCreate
          (
            android.os.Bundle savedInstanceState
          )
          {
            setTitle(R.string.rendering);
            final android.widget.LinearLayout MainLayout = new android.widget.LinearLayout(ctx);
            MainLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
            setContentView(MainLayout);
            TheButtons = new android.widget.RadioGroup(ctx);
            final android.view.ViewGroup.LayoutParams ButtonLayout =
                new android.view.ViewGroup.LayoutParams
                  (
                    android.view.ViewGroup.LayoutParams.FILL_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                  );
              {
                final android.widget.RadioButton RenderingShaded =
                    new android.widget.RadioButton(ctx);
                RenderingShaded.setText(R.string.shaded);
                RenderingShaded.setId(1);
                final android.widget.RadioButton RenderingWireframe =
                    new android.widget.RadioButton(ctx);
                RenderingWireframe.setText(R.string.wireframe);
                RenderingWireframe.setId(0);
                TheButtons.addView(RenderingShaded, 0, ButtonLayout);
                TheButtons.addView(RenderingWireframe, 1, ButtonLayout);
              }
            MainLayout.addView(TheButtons, ButtonLayout);
            TheButtons.check(TheOnScreenView.GetShaded() ? 1 : 0);
            setOnDismissListener(this);
          } /*onCreate*/

        @Override
        public void onDismiss
          (
            android.content.DialogInterface TheDialog
          )
          {
            TheOnScreenView.SetShaded(TheButtons.getCheckedRadioButtonId() != 0);
          } /*onDismiss*/

      } /*RenderingDialog*/

    @Override
    public void onCreate
      (
        android.os.Bundle SavedInstanceState
      )
      {
        super.onCreate(SavedInstanceState);
        setContentView(R.layout.onscreen);
        TheOnScreenView = (OnScreenView)findViewById(R.id.main);
        TheOnScreenView.StatsView = (android.widget.TextView)findViewById(R.id.stats);
        TheOnScreenView.setOnClickListener
          (
            new android.view.View.OnClickListener()
              {
                public void onClick
                  (
                    android.view.View TheView
                  )
                  {
                    new RenderingDialog(OnScreen.this).show();
                  } /*onClick*/
              } /*OnClickListener*/
          );
      } /*onCreate*/

    @Override
    public void onPause()
      {
        super.onPause();
        TheOnScreenView.onPause();
      } /*onPause*/

    @Override
    public void onResume()
      {
        super.onResume();
        TheOnScreenView.onResume();
      } /*onResume*/

  } /*OnScreen*/
