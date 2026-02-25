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
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

/*
 *
 */
@TeleOp(name = "TeleOpNew [TESTING]", group = "Robot")
public class TeleOpNew extends OpMode {

    private DcMotorEx flywheel;
    private CRServo servo;
    private DcMotor leftFront;
    private DcMotor rightFront;
    private DcMotor leftBack;
    private DcMotor rightBack;

    int bankVelocity;

    int shootMs;

    boolean lastPress;

    // This declares the IMU needed to get the current direction the robot is facing
    IMU imu;


    //Timers used
    ElapsedTime shootTimer; // Timer used for shooting
    ElapsedTime telemetryTime; // Timer for telemetry refreshing

    boolean shooting;
    boolean shotActive;

    int telemetryRefresh;

    @Override
    public void init() {

        // Mapping motors
        flywheel = hardwareMap.get(DcMotorEx.class, "flywheel");
        servo = hardwareMap.get(CRServo.class, "servo");
        leftFront = hardwareMap.get(DcMotor.class, "leftFront");
        rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        leftBack = hardwareMap.get(DcMotor.class, "leftBack");
        rightBack = hardwareMap.get(DcMotor.class, "rightBack");

        // Setting the direction and mode for the motors
        flywheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        flywheel.setDirection(DcMotor.Direction.REVERSE);
        servo.setPower(0);

        // Setting mecanum drive
        leftFront.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        leftFront.setDirection(DcMotor.Direction.REVERSE);
        rightFront.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightFront.setDirection(DcMotor.Direction.FORWARD);
        leftBack.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        leftBack.setDirection(DcMotor.Direction.FORWARD);
        rightBack.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightBack.setDirection(DcMotor.Direction.REVERSE);

        // Initializing values
        shooting = false;
        shotActive = false;
        lastPress = false;
        shootTimer = new ElapsedTime();
        shootTimer.reset();
        telemetryTime = new ElapsedTime();
        telemetryTime.reset();

        // Settings
        shootMs = 300; // Time after flywheel spins up to lift servo to release the ball
        bankVelocity = 1300; // Flywheel velocity (1300 default)
        telemetryRefresh = 200; // Telemetry refresh rate in milliseconds (Higher value = better performance)

        imu = hardwareMap.get(IMU.class, "imu");
        // This needs to be changed to match the orientation on your robot
        RevHubOrientationOnRobot.LogoFacingDirection logoDirection =
                RevHubOrientationOnRobot.LogoFacingDirection.UP;
        RevHubOrientationOnRobot.UsbFacingDirection usbDirection =
                RevHubOrientationOnRobot.UsbFacingDirection.FORWARD;

        RevHubOrientationOnRobot orientationOnRobot = new
                RevHubOrientationOnRobot(logoDirection, usbDirection);
        imu.initialize(new IMU.Parameters(orientationOnRobot));
    }

    @Override
    public void loop() {
        // Main logic
        drive(-gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x);
        shoot(shootMs, gamepad1.b);

        // Reset IMU yaw
        if(gamepad1.a){
            imu.resetYaw();
        }

        // TODO: Read IMU then fix telementry

        // Telemetry
        if(telemetryTime.milliseconds() > telemetryRefresh){

            telemetry.addData("Heading (deg)", imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES));
            telemetry.addData("Shooting", shotActive);
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
     */
    private void drive(float yAxisL, float xAxisL, float xAxisR) {

        double lf = yAxisL + xAxisL + xAxisR;
        double rf = yAxisL - xAxisL - xAxisR;
        double lb = yAxisL - xAxisL + xAxisR;
        double rb = yAxisL + xAxisL - xAxisR;

        double max = Math.max(1.0,
                Math.max(Math.abs(lf),
                        Math.max(Math.abs(rf),
                                Math.max(Math.abs(lb), Math.abs(rb)))));

        lf /= max;
        rf /= max;
        lb /= max;
        rb /= max;

        leftFront.setPower(lf);
        rightFront.setPower(rf);
        leftBack.setPower(lb);
        rightBack.setPower(rb);
    }

    /**
     * Checks button to shoot ball
     *
     * @param ms milliseconds for servo to turn after flywheel, ms > 0
     * @param button boolean value for gamepad button to be used
     */
    private void shoot(int ms, boolean button){

        boolean pressed = button && !lastPress;
        lastPress = button;

        if(shotActive){
            if(!shooting && shootTimer.milliseconds() > ms){
                servo.setPower(-1);
                shooting = true;
                shootTimer.reset();
            }
            else if(shooting && shootTimer.milliseconds() > ms){
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
}
