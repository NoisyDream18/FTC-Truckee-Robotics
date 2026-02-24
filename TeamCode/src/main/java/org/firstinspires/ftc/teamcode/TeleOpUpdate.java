package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

@TeleOp(name = "TeleOpUpdateOld")
public class TeleOpUpdate extends LinearOpMode {

    private DcMotor flywheel;
    private CRServo servo;
    private DcMotor leftFront;
    private DcMotor rightFront;
    private DcMotor leftBack;
    private DcMotor rightBack;

    int bankVelocity;
    int farVelocity;

    /**
     * This sample contains the bare minimum Blocks for any regular OpMode. The 3 blue
     * Comment Blocks show where to place Initialization code (runs once, after touching the
     * DS INIT button, and before touching the DS Start arrow), Run code (runs once, after
     * touching Start), and Loop code (runs repeatedly while the OpMode is active, namely not
     * Stopped).
     */
    @Override
    public void runOpMode() {
        int maxVelocity;

        flywheel = hardwareMap.get(DcMotor.class, "flywheel");
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
        // Setting our velocity targets. These values are in ticks per second!
        bankVelocity = 1300;
        farVelocity = 3000;
        maxVelocity = -7000;
        waitForStart();
        while (opModeIsActive()) {
            // Calling our functions while the OpMode is running
            MECANUM_DRIVE();
            setFlywheelVelocity();
            manualServoControl();
            telemetry.addData("Flywheel Velocity", ((DcMotorEx) flywheel).getVelocity());
            telemetry.addData("Flywheel Power", flywheel.getPower());
            telemetry.update();
        }
    }


    private void manualServoControl() {
        // Manual control for the hopper's servo
        if (gamepad1.left_bumper) {
            servo.setPower(-1);
        } else if (gamepad1.right_bumper) {
            servo.setPower(1);
        }
    }


    private void bankShotAuto() {
        ((DcMotorEx) flywheel).setVelocity(bankVelocity);
        servo.setPower(-1);
    }

    /**
     * Describe this function...
     */
    private void farPowerAuto() {
        ((DcMotorEx) flywheel).setVelocity(farVelocity);
        servo.setPower(-1);
    }

    /**
     * Describe this function...
     */
    private void setFlywheelVelocity() {
        if (gamepad1.options) {
            flywheel.setPower(1);
        } else if (gamepad1.dpad_left) {
            farPowerAuto();
        } else if (gamepad1.dpad_right) {
            bankShotAuto();
        } else if (gamepad1.circle) {
            ((DcMotorEx) flywheel).setVelocity(bankVelocity);
        } else if (gamepad1.square) {
            ((DcMotorEx) flywheel).setVelocity(farVelocity);
        } else {
            ((DcMotorEx) flywheel).setVelocity(0);
            // The check below is in place to prevent stuttering with the servo. It checks if the servo is under manual control!
            if (!gamepad1.dpad_right && !gamepad1.dpad_left) {
                servo.setPower(0);
            }
        }
    }

    /**
     * Describe this function...
     */
    private void MECANUM_DRIVE() {
        float forwardBack;
        float strafe;
        float turn;
        float leftFrontPower;
        float rightFrontPower;
        float leftBackPower;
        float rightBackPower;

        // Determining movement based on gamepad inputs
        forwardBack = -gamepad1.left_stick_y;
        strafe = gamepad1.left_stick_x;
        turn = gamepad1.right_stick_x;
        leftFrontPower = forwardBack + strafe + turn;
        rightFrontPower = (forwardBack - strafe) - turn;
        leftBackPower = (forwardBack - strafe) + turn;
        rightBackPower = (forwardBack + strafe) - turn;
        // Setting motor power
        leftFront.setPower(leftFrontPower);
        rightFront.setPower(rightFrontPower);
        leftBack.setPower(leftBackPower);
        rightBack.setPower(rightBackPower);
    }
}
