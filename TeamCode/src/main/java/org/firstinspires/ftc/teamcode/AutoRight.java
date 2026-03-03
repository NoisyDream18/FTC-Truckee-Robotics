package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;

@Autonomous(name = "AutoBlue [TESTING]", group = "Robot")
public class AutoRight extends LinearOpMode {

    private DcMotor rightBack;
    private DcMotor leftFront;
    private DcMotor leftBack;
    private DcMotor rightFront;
    private DcMotor flywheel;
    private CRServo servo;

    int bankVelocity;
    double ticksPerInch;
    double strafeTicksPerInch;
    double ticksPerDegree;

    /**
     * This function is executed when this OpMode is selected from the Driver Station.
     */
    @Override
    public void runOpMode() {
        double wheelDiameterInches;
        double distanceBetweenWheelsInches;
        int farVelocity;

        rightBack = hardwareMap.get(DcMotor.class, "rightBack");
        leftFront = hardwareMap.get(DcMotor.class, "leftFront");
        leftBack = hardwareMap.get(DcMotor.class, "leftBack");
        rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        flywheel = hardwareMap.get(DcMotor.class, "flywheel");
        servo = hardwareMap.get(CRServo.class, "servo");

        // Put initialization blocks here.
        rightBack.setDirection(DcMotor.Direction.REVERSE);
        leftFront.setDirection(DcMotor.Direction.REVERSE);
        leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        flywheel.setDirection(DcMotor.Direction.REVERSE);
        flywheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        servo.setPower(0);
        wheelDiameterInches = 2.95;
        distanceBetweenWheelsInches = 14.96;
        ticksPerInch = (28 / (Math.PI * wheelDiameterInches)) * 24.615384;
        strafeTicksPerInch = ticksPerInch * 0.7273;
        ticksPerDegree = distanceBetweenWheelsInches * Math.sqrt(2) * Math.PI * (ticksPerInch / 360) * 1;
        farVelocity = -3000;
        bankVelocity = 1300;

        //START OF AUTO

        waitForStart();

        moveForward(68, 0.3);
        strafeRight(-6, 0.3);
        rotate(44, 0.1);
        shoot(600);

        //old code

        // moveForward(61, 0.5);
        // rotate(33, 0.1);
        // moveForward(8, 0.5);
        // shoot(1000);


    }

    /**
     * Move forward by distance in Inches
     */
    private void moveForward(int distanceInch, double power) {
        leftBack.setTargetPosition((int) (leftBack.getCurrentPosition() + ticksPerInch * distanceInch));
        leftFront.setTargetPosition((int) (leftFront.getCurrentPosition() + ticksPerInch * distanceInch));
        rightBack.setTargetPosition((int) (rightBack.getCurrentPosition() + ticksPerInch * distanceInch));
        rightFront.setTargetPosition((int) (rightFront.getCurrentPosition() + ticksPerInch * distanceInch));
        leftBack.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        leftFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rightBack.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rightFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        leftBack.setPower(power);
        leftFront.setPower(power);
        rightBack.setPower(power);
        rightFront.setPower(power);
        while (leftBack.isBusy() || leftFront.isBusy() || rightBack.isBusy() || rightFront.isBusy()) {
            telemetry.addLine("Moving");
            telemetry.update();

        }
        leftBack.setPower(0);
        leftFront.setPower(0);
        rightBack.setPower(0);
        rightFront.setPower(0);
    }

    /**
     * Strafe right by distance in Inches
     */
    private void strafeRight(double distanceInch, double power) {
        leftBack.setTargetPosition((int) (leftBack.getCurrentPosition() - strafeTicksPerInch * distanceInch));
        leftFront.setTargetPosition((int) (leftFront.getCurrentPosition() + strafeTicksPerInch * distanceInch));
        rightBack.setTargetPosition((int) (rightBack.getCurrentPosition() + strafeTicksPerInch * distanceInch));
        rightFront.setTargetPosition((int) (rightFront.getCurrentPosition() - strafeTicksPerInch * distanceInch));
        leftBack.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        leftFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rightBack.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rightFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        leftBack.setPower(power);
        leftFront.setPower(power);
        rightBack.setPower(power);
        rightFront.setPower(power);
        while (leftBack.isBusy() || leftFront.isBusy() || rightBack.isBusy() || rightFront.isBusy()) {
            telemetry.addLine("Moving");
            telemetry.update();
        }
        leftBack.setPower(0);
        leftFront.setPower(0);
        rightBack.setPower(0);
        rightFront.setPower(0);
    }


    private void shoot(long delayMs) {
        ElapsedTime t = new ElapsedTime();
        t.reset();

        ((DcMotorEx) flywheel).setVelocity(bankVelocity);
        sleep(delayMs);
        servo.setPower(-1);
        sleep(delayMs);
        ((DcMotorEx) flywheel).setVelocity(0);
        servo.setPower(0);
        sleep(delayMs);



        // t.reset();

        // while (opModeIsActive() && t.milliseconds() < servoWaitMs) {
        //   idle();
        // }


    }

    /**
     * Rotate Counter Clockwise by degrees
     */
    private void rotate(int Angle, double power) {
        leftBack.setTargetPosition((int) (leftBack.getCurrentPosition() - ticksPerDegree * Angle));
        leftFront.setTargetPosition((int) (leftFront.getCurrentPosition() - ticksPerDegree * Angle));
        rightBack.setTargetPosition((int) (rightBack.getCurrentPosition() + ticksPerDegree * Angle));
        rightFront.setTargetPosition((int) (rightFront.getCurrentPosition() + ticksPerDegree * Angle));
        leftBack.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        leftFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rightBack.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rightFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        leftBack.setPower(power);
        leftFront.setPower(power);
        rightBack.setPower(power);
        rightFront.setPower(power);
        while (leftBack.isBusy() || leftFront.isBusy() || rightBack.isBusy() || rightFront.isBusy()) {
            telemetry.addLine("Moving");
            telemetry.update();
        }
        leftBack.setPower(0);
        leftFront.setPower(0);
        rightBack.setPower(0);
        rightFront.setPower(0);
    }


}
