package com.example.countingplugged;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class InstructionsHome extends AppCompatActivity {
    private Button backButton, programOverviewButton, readingFilesButton, paragraphGenerationButton, wordReplacerButton;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_instructions_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        backButton = findViewById(R.id.backButtonInstructions);
        programOverviewButton = findViewById(R.id.buttonProgramOverview);
        readingFilesButton = findViewById(R.id.buttonReadingFiles);
        paragraphGenerationButton = findViewById(R.id.buttonParagraphGeneration);
        wordReplacerButton = findViewById(R.id.buttonWordReplacer);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InstructionsHome.this, HomePage.class);
                startActivity(intent);
            }
        });

        programOverviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InstructionsHome.this, InstructionProgramOverview.class);
                startActivity(intent);
            }
        });

        readingFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InstructionsHome.this, InstructionReadingFiles.class);
                startActivity(intent);
            }
        });

        paragraphGenerationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InstructionsHome.this, InstructionParagraphGeneration.class);
                startActivity(intent);
            }
        });
        wordReplacerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InstructionsHome.this, InstructionWordReplacer.class);
                startActivity(intent);
            }
        });
    }
}