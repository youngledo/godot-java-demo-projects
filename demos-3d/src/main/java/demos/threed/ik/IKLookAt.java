package demos.threed.ik;

import org.godot.annotation.GodotClass;
import org.godot.node.Node3D;
import org.godot.math.Vector3;
import org.godot.math.Basis;
import org.godot.math.Transform3D;

@GodotClass(name = "IKLookAt", parent = "Node3D")
public class IKLookAt extends Node3D {

    private int lookAtAxis = 1;
    private double interpolationValue = 1.0;
    private boolean useOurRotationX = false;
    private boolean useOurRotationY = false;
    private boolean useOurRotationZ = false;
    private boolean useNegativeOurRot = false;
    private Vector3 additionalRotation = new Vector3();
    private boolean positionUsingAdditionalBone = false;
    private String additionalBoneName = "";
    private double additionalBoneLength = 1.0;

    private org.godot.Godot skeletonToUse = null;
    private String boneName = "";
    private int updateMode = 0;
    private boolean firstCall = true;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        Object axisObj = getProperty("look_at_axis");
        if (axisObj instanceof Number) lookAtAxis = ((Number) axisObj).intValue();
        Object interpObj = getProperty("interpolation");
        if (interpObj instanceof Number) interpolationValue = ((Number) interpObj).doubleValue();
        Object useXObj = getProperty("use_our_rotation_x");
        if (useXObj instanceof Boolean) useOurRotationX = (Boolean) useXObj;
        Object useYObj = getProperty("use_our_rotation_y");
        if (useYObj instanceof Boolean) useOurRotationY = (Boolean) useYObj;
        Object useZObj = getProperty("use_our_rotation_z");
        if (useZObj instanceof Boolean) useOurRotationZ = (Boolean) useZObj;
        Object useNegObj = getProperty("use_negative_our_rot");
        if (useNegObj instanceof Boolean) useNegativeOurRot = (Boolean) useNegObj;
        Object addRotObj = getProperty("additional_rotation");
        if (addRotObj instanceof Vector3) additionalRotation = (Vector3) addRotObj;
        Object posAddBoneObj = getProperty("position_using_additional_bone");
        if (posAddBoneObj instanceof Boolean) positionUsingAdditionalBone = (Boolean) posAddBoneObj;
        Object addBoneNameObj = getProperty("additional_bone_name");
        if (addBoneNameObj instanceof String) additionalBoneName = (String) addBoneNameObj;
        Object addBoneLenObj = getProperty("additional_bone_length");
        if (addBoneLenObj instanceof Number) additionalBoneLength = ((Number) addBoneLenObj).doubleValue();
        Object bnObj = getProperty("bone_name");
        if (bnObj instanceof String) boneName = (String) bnObj;
        Object umObj = getProperty("update_mode");
        if (umObj instanceof Number) updateMode = ((Number) umObj).intValue();

        setupSkeletonPath();
    }

    @Override
    public void _process(double delta) {
        if (updateMode == 0) updateSkeleton();
    }

    @Override
    public void _physicsProcess(double delta) {
        if (updateMode == 1) updateSkeleton();
    }

    private void setupSkeletonPath() {
        Object skelPathObj = getProperty("skeleton_path");
        if (skelPathObj == null) return;
        org.godot.Godot temp = (org.godot.Godot) call("get_node", skelPathObj);
        if (temp != null) {
            Object hasMethod = temp.call("has_method", "get_bone_global_pose");
            if (hasMethod instanceof Boolean && (Boolean) hasMethod) {
                skeletonToUse = temp;
            }
        }
    }

    private static Basis makeLookAtBasis(Vector3 forward, Vector3 up) {
        Vector3 f = forward.normalized();
        Vector3 s = up.cross(f).normalized();
        Vector3 u = f.cross(s);
        return new Basis(
                s.x, s.y, s.z,
                u.x, u.y, u.z,
                f.x, f.y, f.z
        );
    }

    private void updateSkeleton() {
        if (firstCall) {
            firstCall = false;
            if (skeletonToUse == null) setupSkeletonPath();
            return;
        }
        if (skeletonToUse == null || updateMode >= 3) return;

        Object boneIdxObj = skeletonToUse.call("find_bone", boneName);
        if (!(boneIdxObj instanceof Number)) return;
        int bone = ((Number) boneIdxObj).intValue();
        if (bone == -1) return;

        Object restObj = skeletonToUse.call("get_bone_global_pose", bone);
        if (!(restObj instanceof Transform3D)) return;
        Transform3D rest = (Transform3D) restObj;

        Vector3 globalOrigin = (Vector3) getProperty("global_position");
        if (globalOrigin == null) return;

        // Convert target to skeleton-local space
        Object skelGlobalObj = skeletonToUse.call("get_global_transform");
        if (skelGlobalObj instanceof Transform3D) {
            Transform3D skelGlobal = (Transform3D) skelGlobalObj;
            globalOrigin = skelGlobal.inverse().apply(globalOrigin);
        }

        Vector3 up;
        if (lookAtAxis == 0) up = Vector3.RIGHT;
        else if (lookAtAxis == 2) up = Vector3.FORWARD;
        else up = Vector3.UP;

        Vector3 dir = globalOrigin.sub(rest.getOrigin()).normalized();
        Basis lookBasis = makeLookAtBasis(dir, up);
        rest = new Transform3D(lookBasis, rest.getOrigin());

        Vector3 restEuler = rest.getBasis().toEuler();
        Vector3 selfEuler = new Vector3();
        Object selfTransObj = call("get_global_transform");
        if (selfTransObj instanceof Transform3D) {
            selfEuler = ((Transform3D) selfTransObj).getBasis().toEuler();
        }

        if (useNegativeOurRot) {
            selfEuler = selfEuler.mul(-1);
        }

        double ex = restEuler.x, ey = restEuler.y, ez = restEuler.z;
        if (useOurRotationX) ex = selfEuler.x;
        if (useOurRotationY) ey = selfEuler.y;
        if (useOurRotationZ) ez = selfEuler.z;

        Basis newBasis = Basis.fromEuler(new Vector3(ex, ey, ez));

        if (additionalRotation.x != 0 || additionalRotation.y != 0 || additionalRotation.z != 0) {
            Vector3 bX = new Vector3(newBasis.xx, newBasis.xy, newBasis.xz);
            Vector3 bY = new Vector3(newBasis.yx, newBasis.yy, newBasis.yz);
            Vector3 bZ = new Vector3(newBasis.zx, newBasis.zy, newBasis.zz);
            if (additionalRotation.x != 0) {
                Basis rotX = Basis.fromAxisAngle(bX, Math.toRadians(additionalRotation.x));
                newBasis = rotX.multiply(newBasis);
            }
            if (additionalRotation.y != 0) {
                Basis rotY = Basis.fromAxisAngle(bY, Math.toRadians(additionalRotation.y));
                newBasis = rotY.multiply(newBasis);
            }
            if (additionalRotation.z != 0) {
                Basis rotZ = Basis.fromAxisAngle(bZ, Math.toRadians(additionalRotation.z));
                newBasis = rotZ.multiply(newBasis);
            }
        }

        rest = new Transform3D(newBasis, rest.getOrigin());

        if (positionUsingAdditionalBone && !additionalBoneName.isEmpty()) {
            Object addBoneIdxObj = skeletonToUse.call("find_bone", additionalBoneName);
            if (addBoneIdxObj instanceof Number) {
                int addBoneIdx = ((Number) addBoneIdxObj).intValue();
                Object addBonePoseObj = skeletonToUse.call("get_bone_global_pose", addBoneIdx);
                if (addBonePoseObj instanceof Transform3D) {
                    Transform3D addBonePose = (Transform3D) addBonePoseObj;
                    Basis addBasis = addBonePose.getBasis();
                    Vector3 zDir = new Vector3(addBasis.zx, addBasis.zy, addBasis.zz).normalized();
                    rest = new Transform3D(rest.getBasis(), addBonePose.getOrigin().sub(zDir.mul(additionalBoneLength)));
                }
            }
        }

        skeletonToUse.call("set_bone_global_pose_override", bone, rest, interpolationValue, true);
    }
}
