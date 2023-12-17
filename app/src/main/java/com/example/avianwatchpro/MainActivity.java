package com.example.avianwatchpro;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.avianwatchpro.ml.BirdsModel;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button btnLoadImage;
    TextView tvResult;
    ImageView ivAddImage;
    ActivityResultLauncher<String> mGetContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivAddImage = findViewById(R.id.imageView);
        tvResult  = findViewById(R.id.tv_output);
        btnLoadImage = findViewById(R.id.btn_load_img);

        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                Bitmap imageBitmap = null;
                try {
                    imageBitmap = UriToBitmap(result);

                } catch (Exception e) {
                    e.printStackTrace();
                }


                ivAddImage.setImageBitmap(imageBitmap);
                outputGenerator(imageBitmap);

                Log.d("TAG_URI"," " + result);
            }
        });


        btnLoadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGetContent.launch("image/*");
            }
        });

        tvResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q="+tvResult.getText()));
                startActivity(intent);
            }
        });
    }



    private Bitmap UriToBitmap(Uri result) throws IOException {
        return MediaStore.Images.Media.getBitmap(this.getContentResolver(),result);
    }

    private void outputGenerator(Bitmap imageBitmap){
        try {
            BirdsModel model = BirdsModel.newInstance(MainActivity.this);

            // Creates inputs for reference.
            TensorImage image = TensorImage.fromBitmap(imageBitmap);

            // Runs model inference and gets result.
            BirdsModel.Outputs outputs = model.process(image);
            List<Category> probability = (List<Category>) outputs.getProbabilityAsCategoryList();

            int index = 0;
            float max = probability.get(0).getScore();
            for (int i = 0; i < probability.size(); i++) {
                if (max < probability.get(i).getScore()){
                    max = probability.get(i).getScore();
                    index = i;
                }
            }

            Category output = probability.get(index);
            tvResult.setText(output.getLabel());

            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }

}