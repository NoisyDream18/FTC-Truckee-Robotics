/* Copyright (c) 2025 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;


/*
 *
 */
@TeleOp(name = "TeleOpNew [TESTING]", group = "Robot")
public class TeleOpNew extends OpMode {

    private DcMotorEx flywheel;
    private CRServo servo;
    private DcMotorEx leftFront;
    private DcMotorEx rightFront;
    private DcMotorEx leftBack;
    private DcMotorEx rightBack;

    int bankVelocity;

    double shootMs;
    double waitMs;

    double fineTurnFactor;
    boolean lastPressShoot;
    boolean lastPressSpeed;

    boolean lastPressSettings;

    // This declares the IMU needed to get the current direction the robot is facing
    IMU imu;

    //Timers used
    ElapsedTime shootTimer; // Timer used for shooting
    ElapsedTime telemetryTime; // Timer for telemetry refreshing

    boolean shooting;
    boolean shotActive;


    double driveSpeedNormalFactor;

    double driveSpeedSlowFactor;

    boolean slowModeEnabled;

    int telemetryRefresh;

    int waitIncrement;

    @Override
    public void init() {

        // Mapping motors
        flywheel = hardwareMap.get(DcMotorEx.class, "flywheel");
        servo = hardwareMap.get(CRServo.class, "servo");
        leftFront = hardwareMap.get(DcMotorEx.class, "leftFront");
        rightFront = hardwareMap.get(DcMotorEx.class, "rightFront");
        leftBack = hardwareMap.get(DcMotorEx.class, "leftBack");
        rightBack = hardwareMap.get(DcMotorEx.class, "rightBack");

        // Initializing servo and flywheel
        flywheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        flywheel.setDirection(DcMotor.Direction.FORWARD);
        servo.setPower(0);

        // Initializing driving motors
        leftFront.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        leftFront.setDirection(DcMotor.Direction.REVERSE);

        rightFront.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightFront.setDirection(DcMotor.Direction.FORWARD);

        leftBack.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        leftBack.setDirection(DcMotor.Direction.FORWARD);

        rightBack.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightBack.setDirection(DcMotor.Direction.REVERSE);

        // BETA for motors
        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Initializing values
        shooting = false;
        shotActive = false;
        lastPressShoot = false;
        lastPressSpeed = false;
        lastPressSettings = false;
        slowModeEnabled = false;
        shootTimer = new ElapsedTime();
        shootTimer.reset();
        telemetryTime = new ElapsedTime();
        telemetryTime.reset();


        // Initializing IMU
        imu = hardwareMap.get(IMU.class, "imu");
        // TODO: This needs to be changed to match the orientation of robot
        RevHubOrientationOnRobot.LogoFacingDirection logoDirection =
                RevHubOrientationOnRobot.LogoFacingDirection.UP;
        RevHubOrientationOnRobot.UsbFacingDirection usbDirection =
                RevHubOrientationOnRobot.UsbFacingDirection.FORWARD;

        RevHubOrientationOnRobot orientationOnRobot = new
                RevHubOrientationOnRobot(logoDirection, usbDirection);
        imu.initialize(new IMU.Parameters(orientationOnRobot));

        // Settings
        shootMs = 1000; // Time for servo to be open to let ball go
        waitMs = 2000; // Time to wait before opening servo
        bankVelocity = 1300; // Flywheel velocity (1300 default)
        telemetryRefresh = 99999999; // Telemetry refresh rate in milliseconds (Higher value = better performance)
        driveSpeedNormalFactor = 1; // Factor for normal movement speed NEVER EXCEED 1
        driveSpeedSlowFactor = driveSpeedNormalFactor/2; // Factor for slow speed NEVER EXCEED 1
        fineTurnFactor = 0.3; // Factor for fine turning when buttons pressed
        waitIncrement = 50; // Increment to change with dpads waitMS for shooting

    }

    @Override
    public void loop() {

        // Main logic
        drive(-gamepad1.left_stick_y, -gamepad1.left_stick_x, gamepad1.right_stick_x, gamepad1.left_bumper, gamepad1.right_bumper);
        shoot(shootMs, waitMs, gamepad1.right_trigger_pressed);


        // Beta feature, toggle slow mode on and off for movement precision / demos
        speed(gamepad1.b);

        if(gamepad1.dpad_up){
            waitMs += waitIncrement;
        } else if(gamepad1.dpad_down){
            waitMs -= waitIncrement;
        }

        // Reset IMU yaw
        if(gamepad1.a){
            imu.resetYaw();
        }

        // TODO: Add fixed turning points for easy rotation?? probably using dpads but may not be nessicary

        // TODO: Read IMU then fix telemetry

        // Telemetry
        if(telemetryTime.milliseconds() > telemetryRefresh){

            telemetry.addData("Heading (deg)", imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES));
            telemetry.addData("Shooting", shotActive);
            telemetry.addData("WaitMs", waitMs);
            // telemetry.addData("Polling",); // TODO: Calculate polling rate FOR TESTING

            telemetry.update();
            telemetryTime.reset();
        }


    }

    /**
     * Calculates motor control for TeleOp movement
     *
     * @param yAxisL Left Joystick Up/Down (Forwards/Backwards)
     * @param xAxisL Left Joystick Left/Right (Strafe)
     * @param xAxisR Right Joystick Left/Right (Turning)
     * @param fineTurnL Button to fine turn left
     * @param fineTurnR Button to fine turn right
     */
    private void drive(float yAxisL, float xAxisL, float xAxisR, boolean fineTurnL, boolean fineTurnR) {

        double turning = xAxisR;

        // XOR where if 1 and only 1 fine turn button is pressed, it overrides joystick turning
        if(fineTurnL ^ fineTurnR) {
            turning = (fineTurnL) ? -fineTurnFactor : fineTurnFactor;
        }


        double lf = yAxisL + xAxisL + turning;
        double rf = yAxisL - xAxisL - turning;
        double lb = yAxisL - xAxisL + turning;
        double rb = yAxisL + xAxisL - turning;

        double max = Math.max(1.0,
                Math.max(Math.abs(lf),
                        Math.max(Math.abs(rf),
                                Math.max(Math.abs(lb), Math.abs(rb)))));


        double speedFactor = slowModeEnabled ? driveSpeedSlowFactor : driveSpeedNormalFactor;

        lf = (lf / max) * speedFactor;
        rf = (rf / max) * speedFactor;
        lb = (lb / max) * speedFactor;
        rb = (rb / max) * speedFactor;


        leftFront.setPower(lf);
        rightFront.setPower(rf);
        leftBack.setPower(lb);
        rightBack.setPower(rb);
    }

    /**
     * Checks button to shoot ball
     *
     *  TODO: ADD PARAMS
     */
    private void shoot(double shootMilliseconds, double waitMilliseconds, boolean button){

        boolean pressed = button && !lastPressShoot;
        lastPressShoot = button;

        if(shotActive){
            if(!shooting && shootTimer.milliseconds() > waitMilliseconds){
                servo.setPower(-1);
                shooting = true;
                shootTimer.reset();
            }
            else if(shooting && shootTimer.milliseconds() > shootMilliseconds){
                shooting = false;
                shotActive = false;
                servo.setPower(0);
                flywheel.setVelocity(0);
                // timer.reset(); // If you want to reset timer after shot ends
            }
        } else if(pressed){
            shootTimer.reset();
            flywheel.setVelocity(bankVelocity);
            shooting = false;
            shotActive = true;
        }
    }

    private void speed(boolean button){

        boolean pressed = button && !lastPressSpeed;
        lastPressSpeed = button;

        if(pressed){
            slowModeEnabled = !slowModeEnabled;
        }
    }


}
