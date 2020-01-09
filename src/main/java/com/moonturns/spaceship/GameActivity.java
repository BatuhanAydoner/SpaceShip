package com.moonturns.spaceship;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.nio.BufferUnderflowException;
import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends AppCompatActivity {

    private final int TIMER_DELAY_MILLISECOND = 0;
    private final int TIMER_PERIOD_MILLISECOND = 20;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private Timer timer;
    private Handler handler;

    private int highScore = 0;
    private int score = 0;
    private boolean finished = false;

    //Tap controls
    private boolean startTap = false; //When player tap firstly, this is true
    private boolean tap = false;

    //View' s X position
    private int pointX = 0;
    private int missileX = 0;
    private int meteoriteX = 0;

    private int pointFirstX = 0;
    private int missileFirstX = 0;
    private int meteoriteFirstX = 0;

    //View' s Y position
    private int spaceShipY = 0;
    private int pointY = 0;
    private int missileY = 0;
    private int meteoriteY = 0;

    private int spaceShipFirstY = 0;
    private int pointFirstY = 0;
    private int missileFirstY = 0;
    private int meteoriteFirstY = 0;

    //View' s width
    private int rootWidth = 0;
    private int spaceShipWidth = 0;
    private int pointWidth = 0;
    private int missileWidth = 0;
    private int meteoriteWidth = 0;

    //View' s height
    private int rootHeight = 0;
    private int spaceShipHeight = 0;
    private int pointHeight = 0;
    private int missileHeight = 0;
    private int meteoriteHeight = 0;

    //View' s speeds
    private int spaceShipSpeed = 0;
    private int pointSpeed = 0;
    private int missileSpeed = 0;
    private int meteoriteSpeed = 0;


    private ConstraintLayout root;
    private ImageView imgSpaceShip, imgPoint, imgMissile, imgMeteorite, imgReplay;
    private TextView txtPlay, txtScore, txtHighScore;

    //init views
    private void crt() {
        root = this.findViewById(R.id.root);
        imgSpaceShip = this.findViewById(R.id.imgSpaceShip);
        imgPoint = this.findViewById(R.id.imgPoint);
        imgMissile = this.findViewById(R.id.imgMissile);
        imgMeteorite = this.findViewById(R.id.imgMeteorite);
        imgReplay = this.findViewById(R.id.imgReplay);
        txtPlay = this.findViewById(R.id.txtPlay);
        txtScore = this.findViewById(R.id.txtScore);
        txtHighScore = this.findViewById(R.id.txtHighScore);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        crt();
        eventTap();
        eventImgReplay();
        setPreferences();
    }

    private void eventTap() {
        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (startTap) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        tap = true;
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        tap = false;
                    }
                } else {
                    txtPlay.setVisibility(View.GONE);
                    txtHighScore.setVisibility(View.GONE);
                    getViewsPositionX();
                    getViewsPositionY();
                    getViewsWidth();
                    getViewsHeight();
                    startTap = true;
                    imgSpaceShip.setY(spaceShipY);
                    handler = new Handler();
                    setTimer();
                    controlObjects();
                }

                return true;
            }
        });
    }

    private void eventImgReplay() {
        imgReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replay();
            }
        });
    }

    private void setTimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                spaceShipMoving();
                objectsMoves();
                controlCollision();
            }
        }, TIMER_DELAY_MILLISECOND, TIMER_PERIOD_MILLISECOND);
    }

    //imgSpaceShip up and down moves
    private void spaceShipMoving() {
        spaceShipSpeed = Math.round(rootHeight / 60);
        if (tap) {
            spaceShipY -= spaceShipSpeed;
        } else {
            spaceShipY += spaceShipSpeed;
        }

        if (spaceShipY <= 0) {
            spaceShipY = 0;
        }

        if (spaceShipY >= rootHeight - spaceShipHeight) {
            spaceShipY = rootHeight - spaceShipHeight;
        }

        imgSpaceShip.setY(spaceShipY);
    }

    //Objects moves
    private void objectsMoves() {
        pointSpeed = Math.round(rootWidth / 60);
        missileSpeed = Math.round(rootWidth / 60);
        meteoriteSpeed = Math.round(rootWidth / 60);

        pointX -= pointSpeed;
        if (pointX < 0) {
            pointX = rootWidth + pointSpeed;
            pointY = (int) Math.floor(rootHeight * Math.random());
            if (pointY >= rootHeight) {
                pointY = rootHeight - pointHeight;
            } else if (pointY <= 0) {
                pointY = 0;
            }
        }

        imgPoint.setX(pointX);
        imgPoint.setY(pointY);

        missileX -= missileSpeed;
        if (missileX < 0) {
            missileX = rootWidth + missileSpeed;
            missileY = (int) Math.floor(rootHeight * Math.random());
            if (missileY >= rootHeight) {
                missileY = rootHeight - missileHeight;
            } else if (missileY <= 0) {
                missileY = 0;
            }
        }

        imgMissile.setX(missileX);
        imgMissile.setY(missileY);

        meteoriteX -= meteoriteSpeed;
        if (meteoriteX < 0) {
            meteoriteX = rootWidth + meteoriteSpeed;
            meteoriteY = (int) Math.floor(rootHeight * Math.random());
            if (meteoriteY >= rootHeight) {
                meteoriteY = rootHeight - meteoriteHeight;
            } else if (meteoriteY <= 0) {
                meteoriteY = 0;
            }
        }

        imgMeteorite.setX(meteoriteX);
        imgMeteorite.setY(meteoriteY);
    }

    //Objects collision
    private void controlCollision() {
        int pointCenterX = pointX + pointWidth / 2;
        int pointCenterY = pointY + pointHeight / 2;

        if (pointCenterX > 0 && pointCenterX <= spaceShipWidth && pointCenterY >= spaceShipY && pointCenterY <= spaceShipY + spaceShipHeight) {
            score += 50;
            pointX = -10;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    txtScore.setText(String.valueOf(score));
                }
            });

        }

        imgPoint.setX(pointX);

        int missileCenterX = missileX + missileWidth / 2;
        int missileCenterY = missileY + missileHeight / 2;

        if (missileCenterX > 0 && missileCenterX <= spaceShipWidth - 20 && missileCenterY >= spaceShipY && missileCenterY <= spaceShipY + spaceShipHeight - 25) {
            finished = true;
            timer.cancel();
            finishedGame();
        }

        int meteoriteCenterX = meteoriteX + meteoriteWidth / 2;
        int meteoriteCenterY = meteoriteY + meteoriteHeight / 2;

        if (meteoriteCenterX > 0 && meteoriteCenterX <= spaceShipWidth - 20 && meteoriteCenterY >= spaceShipY && meteoriteCenterY <= spaceShipY + spaceShipHeight - 25) {
            finished = true;
            timer.cancel();
            finishedGame();
        }
    }

    private void getViewsPositionX() {
        pointX = (int) imgPoint.getX();
        missileX = (int) imgMissile.getX();
        meteoriteX = (int) imgMeteorite.getX();

        if (!startTap) {
            pointFirstX = pointX;
            missileFirstX = missileX;
            meteoriteFirstX = meteoriteX;
        }

    }

    private void getViewsPositionY() {
        spaceShipY = (int) imgSpaceShip.getY();
        pointY = (int) imgPoint.getY();
        missileY = (int) imgMissile.getY();
        meteoriteY = (int) imgMeteorite.getY();

        if (!startTap) {
            spaceShipFirstY = spaceShipY;
            pointFirstY = spaceShipY;
            missileFirstY = spaceShipY;
            meteoriteFirstY = spaceShipY;
        }
    }

    private void getViewsWidth() {
        rootWidth = root.getWidth();
        spaceShipWidth = imgSpaceShip.getWidth();
        pointWidth = imgPoint.getWidth();
        missileWidth = imgMissile.getWidth();
        meteoriteWidth = imgMeteorite.getWidth();
    }

    private void getViewsHeight() {
        rootHeight = root.getHeight();
        spaceShipHeight = imgSpaceShip.getHeight();
        pointHeight = imgPoint.getHeight();
        missileHeight = imgMissile.getHeight();
        meteoriteHeight = imgMeteorite.getHeight();
    }

    private void controlObjects() {
        imgPoint.setVisibility(View.VISIBLE);
        imgMissile.setVisibility(View.VISIBLE);
        imgMeteorite.setVisibility(View.VISIBLE);
    }

    private void replay() {
        score = 0;
        txtScore.setText(String.valueOf(score));
        imgReplay.setVisibility(View.GONE);
        txtHighScore.setVisibility(View.GONE);
        finished = false;

        pointX = pointFirstX;
        missileX = missileFirstX;
        meteoriteX = meteoriteFirstX;
        pointY = pointFirstY;
        missileY = missileFirstY;
        meteoriteY = meteoriteFirstY;
        spaceShipY = spaceShipFirstY;
        setTimer();

    }

    private void setPreferences() {
        preferences = this.getSharedPreferences("high_score", MODE_PRIVATE);
        editor = preferences.edit();

        if (preferences.contains("high_score")) {
            txtHighScore.setVisibility(View.VISIBLE);

            highScore = preferences.getInt("high_score", 0);

            txtHighScore.setText("High score : " + highScore);
        } else {

        }

    }

    private void setScore() {
        highScore = preferences.getInt("high_score", 0);

        if (score > highScore) {
            editor.putInt("high_score", score);
            editor.commit();
            highScore = score;
        }
    }

    private void finishedGame() {
        if (finished) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    timer = null;
                    setScore();
                    txtHighScore.setVisibility(View.VISIBLE);
                    txtHighScore.setText("High score : " + highScore);
                    imgReplay.setVisibility(View.VISIBLE);
                }
            });
        }
    }
}
