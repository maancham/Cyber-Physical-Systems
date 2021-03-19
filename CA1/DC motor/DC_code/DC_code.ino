int inPinOnOff = 2;
int inPinDec = 3;
int inPinInc = 4;
int inPinChangeDir = 5;
int outPinDC1 = 9;
int outPinDC2 = 10;
int isFunctional = 0;
int Maxpwm = 255;
int Minpwm = 0;
int velocity = Maxpwm/2;
int pwm_cnt = 0;
char spin_direction = 'r';

void setup() {
  // denoting input pins for setup:
  pinMode(inPinOnOff, INPUT); 
  pinMode(inPinDec, INPUT); 
  pinMode(inPinInc, INPUT); 
  pinMode(inPinChangeDir, INPUT);

  // denoting output pins for setup:
  pinMode(outPinDC1,  OUTPUT); 
  pinMode(outPinDC2, OUTPUT);


}

void turnRight(){
  digitalWrite(outPinDC1, HIGH);
  digitalWrite(outPinDC2, LOW);
}

void turnLeft(){
  digitalWrite(outPinDC1, LOW);
  digitalWrite(outPinDC2, HIGH);
}

void turnMotor(char dir){
  if (dir == 'r') {
    turnRight();
  }
  else {
    turnLeft();
  }
}

void stopMotor(){
  digitalWrite(outPinDC1, LOW);
  digitalWrite(outPinDC2, LOW);
}



void loop() {
  // Reading the current state of all keys:
  int InitKeyVal = digitalRead(inPinOnOff);
  int DecKeyVal = digitalRead(inPinDec);
  int IncKeyVal = digitalRead(inPinInc);
  int ChangeKeyVal = digitalRead(inPinChangeDir);

  if (isFunctional == 1) {
    if (pwm_cnt <= velocity) {
      turnMotor(spin_direction);
    }
    else {
      stopMotor();
    } 
  }

  if (isFunctional == 1) {
    if (InitKeyVal == LOW) {
      while (digitalRead(inPinOnOff) == LOW){};
      isFunctional = 0;
      stopMotor();
    }

    else if (IncKeyVal == LOW) {
      while (digitalRead(inPinInc) == LOW){};
      velocity = velocity + 10;
      if (velocity >= Maxpwm) {
        velocity = Maxpwm;
      }
    }

    else if (DecKeyVal == LOW) {
        while (digitalRead(inPinDec) == LOW){};
        velocity = velocity - 10;
        if (velocity <= Minpwm) {
          velocity = Minpwm;
        }
    }

    else if (ChangeKeyVal == LOW) {
        while (digitalRead(inPinChangeDir) == LOW){};
        if (spin_direction == 'r') {
          spin_direction = 'l';
        }
        else {
          spin_direction = 'r';
        }
    }
  }
  
  else {
    if (InitKeyVal == LOW) {
      while (digitalRead(inPinOnOff) == LOW){};
      isFunctional = 1;
    }
  }

  pwm_cnt = (pwm_cnt + 1) % (Maxpwm - Minpwm);



  // handling Off mode:
//  if (isFunctional == 0) {
//    Serial.println(Minpwm);
//    if (spin_direction == 'r') {
//       analogWrite(outPinDC1, Minpwm);
//    }
//    else {
//      analogWrite(outPinDC2, Minpwm);
//    }    
//  }
//  // handling On mode:
//  else if (isFunctional == 1) {
//    Serial.println(velocity);
//    if (spin_direction == 'r') {
//      analogWrite(outPinDC1, velocity);
//      analogWrite(outPinDC2, Minpwm);
//    }
//    else {
//      analogWrite(outPinDC1, Minpwm);
//      analogWrite(outPinDC2, velocity);
//    }
//  }
//
//  // Start and Pause of DC motor:
//  if (InitKeyVal == LOW) {
//    while (digitalRead(inPinOnOff) == LOW){};
//    if (isFunctional == 0) {
//      isFunctional = 1;
//    }
//    else {
//      isFunctional = 0;
//    }
//  }
//
//  // Increasing total velocity of DC motor:
//  else if (IncKeyVal == LOW) {
//    if (isFunctional == 1) {
//      while (digitalRead(inPinInc) == LOW){};
//      velocity = velocity + 10;
//      if (velocity >= Maxpwm) {
//        velocity = Maxpwm;
//      }
//      Serial.println(velocity);
//    } 
//  }
//
//  // Decreasing total velocity of DC motor:
//  else if (DecKeyVal == LOW) {
//    if (isFunctional == 1) {
//      while (digitalRead(inPinDec) == LOW){};
//      velocity = velocity - 10;
//      if (velocity <= Minpwm) {
//        velocity = Minpwm;
//      }
//      Serial.println(velocity);
//    } 
//  }
//
//  // Changing the Spin Rotation of DC motor:
//  else if (ChangeKeyVal == LOW) {
//    if (isFunctional == 1) {
//      while (digitalRead(inPinChangeDir) == LOW){};
//      if (spin_direction == 'r') {
//        spin_direction = 'l';
//      }
//      else {
//        spin_direction = 'r';
//      }
//      Serial.println(velocity);
//    }
//  }

}
