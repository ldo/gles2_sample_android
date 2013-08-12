package nz.gen.geek_central.gles2_sample;
/*
    GLES 2.0 sample--mainline.

    Copyright 2012, 2013 by Lawrence D'Oliveiro <ldo@geek-central.gen.nz>.

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

public class Main extends android.app.Activity
  {
    OnScreenView TheOnScreenView;

    class AnimationsDialog
        extends android.app.Dialog
        implements android.content.DialogInterface.OnDismissListener
      {
        final android.content.Context ctx;
        android.widget.RadioGroup TheButtons;

        public AnimationsDialog
          (
            android.content.Context ctx
          )
          {
            super(ctx);
            this.ctx = ctx;
          } /*AnimationsDialog*/

        @Override
        public void onCreate
          (
            android.os.Bundle savedInstanceState
          )
          {
            setTitle(R.string.choose_animation);
            final android.widget.LinearLayout MainLayout = new android.widget.LinearLayout(ctx);
            MainLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
            setContentView(MainLayout);
            TheButtons = new android.widget.RadioGroup(ctx);
            final android.view.ViewGroup.LayoutParams ButtonLayout =
                new android.view.ViewGroup.LayoutParams
                  (
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                  );
            for (OnScreenView.Animations Animation : OnScreenView.Animations.values())
              {
                final android.widget.RadioButton ThisChoice = new android.widget.RadioButton(ctx);
                ThisChoice.setText(Animation.NameID);
                ThisChoice.setId(Animation.NameID);
                TheButtons.addView(ThisChoice, TheButtons.getChildCount(), ButtonLayout);
              }
            MainLayout.addView(TheButtons, ButtonLayout);
            TheButtons.check(TheOnScreenView.GetAnimation().NameID);
            setOnDismissListener(this);
          } /*onCreate*/

        @Override
        public void onDismiss
          (
            android.content.DialogInterface TheDialog
          )
          {
            TheOnScreenView.SetAnimation
              (
                OnScreenView.Animations.WithName(TheButtons.getCheckedRadioButtonId())
              );
          } /*onDismiss*/

      } /*AnimationsDialog*/

    @Override
    public void onCreate
      (
        android.os.Bundle ToRestore
      )
      {
        super.onCreate(ToRestore);
        setContentView(R.layout.main);
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
                    new AnimationsDialog(Main.this).show();
                  } /*onClick*/
              } /*OnClickListener*/
          );
      } /*onCreate*/

    @Override
    public void onPause()
      {
        TheOnScreenView.onPause();
        super.onPause();
      } /*onPause*/

    @Override
    public void onResume()
      {
        super.onResume();
        TheOnScreenView.onResume();
      } /*onResume*/

  } /*Main*/;
