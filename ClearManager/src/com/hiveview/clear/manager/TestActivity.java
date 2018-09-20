package com.hiveview.clear.manager;
 
import android.animation.AnimatorSet;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class TestActivity extends Activity{

	
	private Button button1;
	private Button button2;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_activity);
		
		
		button1 = (Button)findViewById(R.id.one_key_clear);
		button1.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
			    Toast.makeText(TestActivity.this, "button1", 1000).show();	
			}
		});
		button2 = (Button)findViewById(R.id.manual_clear);
		button2.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				 Toast.makeText(TestActivity.this, "button2", 1000).show();	
			}
		});
		
		button1.setEnabled(false);
		button2.setEnabled(false);
	}
	
	
	private AnimatorSet initDownArrow(ImageView arrow1, ImageView arrow2, ImageView arrow3) {

        float translationYHalf = 40;
        float translationY = 20;

        Keyframe translation1 = Keyframe.ofFloat(0f, 0.0f);
        Keyframe translation2 = Keyframe.ofFloat(0.764f, 0.0f);
        Keyframe translation3 = Keyframe.ofFloat(0.882f, translationYHalf);
        Keyframe translation4 = Keyframe.ofFloat(1f, translationY);

        Keyframe alpha0 = Keyframe.ofFloat(0, 0.0f);
        Keyframe alpha1 = Keyframe.ofFloat(0, 1.0f);
        Keyframe alpha2 = Keyframe.ofFloat(0.764f, 0.0f);
        Keyframe alpha3 = Keyframe.ofFloat(0.882f, 1.0f);

        Keyframe alpha4 = Keyframe.ofFloat(1.0f, 1.0f);
        Keyframe alpha5 = Keyframe.ofFloat(0.764f, 1.0f);
        Keyframe alpha6 = Keyframe.ofFloat(0.882f, 0.0f);
        Keyframe alpha7 = Keyframe.ofFloat(1.0f, 0.0f);

        PropertyValuesHolder arrow1YHolder = PropertyValuesHolder.ofKeyframe("translationY", translation1, translation2, translation3, translation4);
        PropertyValuesHolder arrow1AlphaHolder = PropertyValuesHolder.ofKeyframe("alpha", alpha1, alpha5, alpha6, alpha7);
        ObjectAnimator move1 = ObjectAnimator.ofPropertyValuesHolder(arrow1, arrow1YHolder, arrow1AlphaHolder);

        PropertyValuesHolder arrow2YHolder = PropertyValuesHolder.ofKeyframe("translationY", translation1, translation2, translation3, translation4);
        PropertyValuesHolder arrow2AlphaHolder = PropertyValuesHolder.ofKeyframe("alpha", alpha0, alpha2, alpha3, alpha4);
        ObjectAnimator move2 = ObjectAnimator.ofPropertyValuesHolder(arrow2, arrow2YHolder, arrow2AlphaHolder);


        PropertyValuesHolder arrow3YHolder = PropertyValuesHolder.ofKeyframe("translationY", translation1, translation1, translation1, translation1);
        PropertyValuesHolder arrow3AlphaHolder = PropertyValuesHolder.ofKeyframe("alpha", alpha0, alpha0, alpha0, alpha0);
        ObjectAnimator move3 = ObjectAnimator.ofPropertyValuesHolder(arrow3, arrow3YHolder, arrow3AlphaHolder);

        move1.setInterpolator(new LinearInterpolator());
        move1.setRepeatCount(Animation.INFINITE);
        move1.setRepeatMode(Animation.RESTART);

        move2.setInterpolator(new LinearInterpolator());
        move2.setRepeatCount(Animation.INFINITE);
        move2.setRepeatMode(Animation.RESTART);

        AnimatorSet animator = new AnimatorSet();
        animator.setDuration(2000);
        animator.playTogether(move3, move1, move2);

        return animator;
    }

}
