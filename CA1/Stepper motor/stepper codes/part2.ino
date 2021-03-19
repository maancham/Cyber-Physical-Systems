#include <Stepper.h>

#define STEP_ROTATE_DEG 10
#define STEPS_PER_REVOLUTION 360 / STEP_ROTATE_DEG
#define RPM 1


int dir = 1;
const int btn_cw = 3;
const int btn_ccw = 4;
const int btn_off = 5;
const int stepper_A1 = 13;
const int stepper_A2 = 12;
const int stepper_B1 = 11;
const int stepper_B2 = 10;
// int prev_step = 0;

Stepper stepper_motor(STEPS_PER_REVOLUTION, stepper_A1,stepper_A2,stepper_B1,stepper_B2);

void setup(){
    stepper_motor.setSpeed(RPM);
    pinMode(btn_ccw,INPUT);
    pinMode(btn_cw,INPUT);
    pinMode(btn_off,INPUT);
}

void loop(){
    //percept
    int btn_cw_val = digitalRead(btn_cw);
    int btn_ccw_val = digitalRead(btn_ccw);
    int btn_off_val = digitalRead(btn_off);
    //update state
    if (btn_cw_val == LOW){
        dir = 1;
    }
    if (btn_ccw_val == LOW) {
        dir = -1;
    }
    if (btn_off_val == LOW){
        dir = 0;
    }
    // actuate
    if (dir != 0){
        stepper_motor.step(dir);
    }  
}
