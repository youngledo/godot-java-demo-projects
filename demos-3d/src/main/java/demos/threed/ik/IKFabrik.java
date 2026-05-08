package demos.threed.ik;

import org.godot.annotation.GodotClass;
import org.godot.node.Node3D;
import org.godot.math.Vector3;
import org.godot.math.Basis;
import org.godot.math.Transform3D;
import org.godot.node.Node;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@GodotClass(name = "IKFabrik", parent = "Node3D")
public class IKFabrik extends Node3D {

    private static final double CHAIN_TOLERANCE = 0.01;
    private static final int CHAIN_MAX_ITER = 10;

    private org.godot.node.Skeleton3D skeleton;
    private org.godot.node.Node targetNode;
    private org.godot.node.Node middleJointTarget;

    private String[] bonesInChain = new String[0];
    private double[] bonesInChainLengths = new double[0];
    private Map<String, Integer> boneIDs = new HashMap<>();
    private ArrayList<org.godot.Godot> boneNodes = new ArrayList<>();

    private Vector3 chainOrigin = new Vector3();
    private double totalLength = Double.POSITIVE_INFINITY;
    private int chainIterations = 0;
    private boolean limitChainIterations = true;
    private boolean resetIterationsOnUpdate = false;
    private boolean useMiddleJointTarget = false;

    private int updateMode = 0;
    private boolean firstCall = true;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        Object bonesObj = getProperty("bones_in_chain");
        if (bonesObj instanceof String[]) bonesInChain = (String[]) bonesObj;
        else if (bonesObj instanceof Object[]) {
            Object[] arr = (Object[]) bonesObj;
            bonesInChain = new String[arr.length];
            for (int i = 0; i < arr.length; i++) bonesInChain[i] = arr[i].toString();
        }

        Object lengthsObj = getProperty("bones_in_chain_lengths");
        if (lengthsObj instanceof double[]) bonesInChainLengths = (double[]) lengthsObj;
        else if (lengthsObj instanceof float[]) {
            float[] arr = (float[]) lengthsObj;
            bonesInChainLengths = new double[arr.length];
            for (int i = 0; i < arr.length; i++) bonesInChainLengths[i] = arr[i];
        } else if (lengthsObj instanceof Number[]) {
            Number[] arr = (Number[]) lengthsObj;
            bonesInChainLengths = new double[arr.length];
            for (int i = 0; i < arr.length; i++) bonesInChainLengths[i] = arr[i].doubleValue();
        }

        Object limitObj = getProperty("limit_chain_iterations");
        if (limitObj instanceof Boolean) limitChainIterations = (Boolean) limitObj;

        Object resetObj = getProperty("reset_iterations_on_update");
        if (resetObj instanceof Boolean) resetIterationsOnUpdate = (Boolean) resetObj;

        Object midJointObj = getProperty("use_middle_joint_target");
        if (midJointObj instanceof Boolean) useMiddleJointTarget = (Boolean) midJointObj;

        Object umObj = getProperty("update_mode");
        if (umObj instanceof Number) updateMode = ((Number) umObj).intValue();

        setupSkeletonPath();

        if (!(boolean) call("has_node", "Target")) {
            org.godot.node.Node newTarget = (org.godot.node.Node) call("create_node", "Node3D");
            newTarget.setName("Target");
            addChild((org.godot.node.Node) newTarget);
            targetNode = (org.godot.node.Node) newTarget;
        } else {
            targetNode = getNode("Target");
        }

        if (!(boolean) call("has_node", "MiddleJoint")) {
            org.godot.node.Node newMid = (org.godot.node.Node) call("create_node", "Node3D");
            newMid.setName("MiddleJoint");
            addChild((org.godot.node.Node) newMid);
            middleJointTarget = (org.godot.node.Node) newMid;
        } else {
            middleJointTarget = getNode("MiddleJoint");
        }

        makeBoneNodes();
    }

    @Override
    public void _process(double delta) {
        if (updateMode != 0) return;
        if (resetIterationsOnUpdate) chainIterations = 0;
        updateSkeleton();
    }

    @Override
    public void _physicsProcess(double delta) {
        if (updateMode != 1) return;
        if (resetIterationsOnUpdate) chainIterations = 0;
        updateSkeleton();
    }

    private static Basis makeLookAtBasis(Vector3 forward, Vector3 up) {
        Vector3 f = forward.normalized();
        Vector3 s = up.cross(f).normalized();
        Vector3 u = f.cross(s);
        return new Basis(s.x, s.y, s.z, u.x, u.y, u.z, f.x, f.y, f.z);
    }

    private void setupSkeletonPath() {
        Object skelPathObj = getProperty("skeleton_path");
        if (skelPathObj == null) return;

        org.godot.node.Node temp = getNode((String) skelPathObj);
        if (temp != null) {
            Object hasMethod = temp.hasMethod("get_bone_global_pose");
            if (hasMethod instanceof Boolean && (Boolean) hasMethod) {
                skeleton = (org.godot.node.Skeleton3D) temp;
                boneIDs.clear();
                makeBoneNodes();
            }
        }
    }

    private void makeBoneNodes() {
        boneNodes.clear();
        for (int i = 0; i < bonesInChain.length; i++) {
            String boneName = bonesInChain[i];
            if (!(boolean) call("has_node", boneName)) {
                org.godot.node.Node newNode = (org.godot.node.Node) call("create_node", "Node3D");
                newNode.setName(boneName);
                addChild((org.godot.node.Node) newNode);
                boneNodes.add(newNode);
            } else {
                boneNodes.add(getNode(boneName));
            }
        }
    }

    private void updateSkeleton() {
        if (firstCall) {
            firstCall = false;
            if (skeleton == null) setupSkeletonPath();
            return;
        }
        if (skeleton == null) return;
        if (bonesInChain.length == 0 || bonesInChainLengths.length == 0) return;
        if (bonesInChain.length != bonesInChainLengths.length) return;

        if (boneIDs.isEmpty()) {
            for (int i = 0; i < bonesInChain.length; i++) {
                Object idxObj = skeleton.findBone(bonesInChain[i]);
                if (idxObj instanceof Number) {
                    boneIDs.put(bonesInChain[i], ((Number) idxObj).intValue());
                }
                if (i < boneNodes.size()) {
                    Transform3D bt = getBoneTransform(i, true);
                    if (bt != null) {
                        boneNodes.get(i).call("set_global_transform", bt);
                    }
                    if (i < boneNodes.size() - 1) {
                        Transform3D nextBt = getBoneTransform(i + 1, true);
                        if (nextBt != null) {
                            Object skelOrigin = skeleton.getGlobalPosition();
                            Vector3 lookTarget = nextBt.getOrigin();
                            if (skelOrigin instanceof Vector3) {
                                lookTarget = lookTarget.add((Vector3) skelOrigin);
                            }
                            boneNodes.get(i).call("look_at", lookTarget, Vector3.UP);
                        }
                    }
                }
            }
        }

        if (totalLength == Double.POSITIVE_INFINITY) {
            totalLength = 0;
            for (double l : bonesInChainLengths) totalLength += l;
        }

        solveChain();
    }

    private void solveChain() {
        if (chainIterations >= CHAIN_MAX_ITER && limitChainIterations) return;
        chainIterations = 0;

        Transform3D originTf = getBoneTransform(0, true);
        if (originTf != null) chainOrigin = originTf.getOrigin();

        Vector3 dir;
        if (boneNodes.size() > 2) {
            Object prevTfObj = boneNodes.get(boneNodes.size() - 2).call("get_global_transform");
            if (prevTfObj instanceof Transform3D) {
                Basis b = ((Transform3D) prevTfObj).getBasis();
                dir = new Vector3(b.zx, b.zy, b.zz).normalized();
            } else {
                dir = Vector3.FORWARD;
            }
        } else {
            if (targetNode != null) {
                Object targetTfObj = targetNode.call("get_global_transform");
                if (targetTfObj instanceof Transform3D) {
                    Basis b = ((Transform3D) targetTfObj).getBasis();
                    dir = new Vector3(b.zx, b.zy, b.zz).mul(-1).normalized();
                } else {
                    dir = Vector3.FORWARD;
                }
            } else {
                dir = Vector3.FORWARD;
            }
        }

        Vector3 targetPos = Vector3.ZERO;
        if (targetNode != null) {
            targetPos = (Vector3) targetNode.call("get_global_position");
            if (targetPos == null) targetPos = Vector3.ZERO;
        }
        targetPos = targetPos.add(dir.mul(bonesInChainLengths[boneNodes.size() - 1]));

        if (useMiddleJointTarget && boneNodes.size() > 2 && middleJointTarget != null) {
            Vector3 midPos = (Vector3) middleJointTarget.call("get_global_position");
            Vector3 boneMidPos = (Vector3) boneNodes.get(boneNodes.size() / 2).call("get_global_position");
            if (midPos != null && boneMidPos != null) {
                Vector3 diff = midPos.sub(boneMidPos);
                Vector3 normalized = diff.normalized();
                Object midGlobalObj = boneNodes.get(boneNodes.size() / 2).call("get_global_transform");
                if (midGlobalObj instanceof Transform3D) {
                    Transform3D midGlobal = (Transform3D) midGlobalObj;
                    Vector3 newOrigin = midGlobal.getOrigin().add(normalized);
                    boneNodes.get(boneNodes.size() / 2).call("set_global_transform",
                            new Transform3D(midGlobal.getBasis(), newOrigin));
                }
            }
        }

        Vector3 endEffectorPos = (Vector3) boneNodes.get(boneNodes.size() - 1).call("get_global_position");
        if (endEffectorPos == null) endEffectorPos = Vector3.ZERO;
        double dif = endEffectorPos.sub(targetPos).length();

        while (dif > CHAIN_TOLERANCE) {
            chainBackward(targetPos, dir);
            chainForward();
            chainApplyRotation();

            endEffectorPos = (Vector3) boneNodes.get(boneNodes.size() - 1).call("get_global_position");
            if (endEffectorPos == null) endEffectorPos = Vector3.ZERO;
            dif = endEffectorPos.sub(targetPos).length();

            chainIterations++;
            if (chainIterations >= CHAIN_MAX_ITER) break;
        }

        for (int i = 0; i < boneNodes.size(); i++) {
            Transform3D resetTf = getBoneTransform(i, true);
            if (resetTf != null) {
                boneNodes.get(i).call("set_global_transform", resetTf);
            }
        }
    }

    private Vector3 getNodeOrigin(org.godot.Godot node) {
        Object tfObj = node.call("get_global_transform");
        if (tfObj instanceof Transform3D) return ((Transform3D) tfObj).getOrigin();
        return (Vector3) node.call("get_global_position");
    }

    private void setNodeOrigin(org.godot.Godot node, Vector3 origin) {
        Object tfObj = node.call("get_global_transform");
        if (tfObj instanceof Transform3D) {
            Transform3D tf = (Transform3D) tfObj;
            node.call("set_global_transform", new Transform3D(tf.getBasis(), origin));
        }
    }

    private void chainBackward(Vector3 targetPos, Vector3 dir) {
        Vector3 endPos = targetPos.add(dir.mul(bonesInChainLengths[boneNodes.size() - 1]));
        setNodeOrigin(boneNodes.get(boneNodes.size() - 1), endPos);

        for (int i = bonesInChain.length - 1; i >= 1; i--) {
            Vector3 prevOrigin = getNodeOrigin(boneNodes.get(i));
            Vector3 currOrigin = getNodeOrigin(boneNodes.get(i - 1));

            Vector3 r = prevOrigin.sub(currOrigin);
            double rLen = r.length();
            if (rLen < 0.0001) continue;
            double l = bonesInChainLengths[i - 1] / rLen;
            Vector3 newPos = prevOrigin.lerp(currOrigin, l);
            setNodeOrigin(boneNodes.get(i - 1), newPos);
        }
    }

    private void chainForward() {
        setNodeOrigin(boneNodes.get(0), chainOrigin);

        for (int i = 0; i < bonesInChain.length - 1; i++) {
            Vector3 currOrigin = getNodeOrigin(boneNodes.get(i));
            Vector3 nextOrigin = getNodeOrigin(boneNodes.get(i + 1));

            Vector3 r = nextOrigin.sub(currOrigin);
            double rLen = r.length();
            if (rLen < 0.0001) continue;
            double l = bonesInChainLengths[i] / rLen;
            Vector3 newPos = currOrigin.lerp(nextOrigin, l);
            setNodeOrigin(boneNodes.get(i + 1), newPos);
        }
    }

    private void chainApplyRotation() {
        Vector3 skelOrigin = Vector3.ZERO;
        Object skelPosObj = skeleton.getGlobalPosition();
        if (skelPosObj instanceof Vector3) skelOrigin = (Vector3) skelPosObj;

        for (int i = 0; i < bonesInChain.length; i++) {
            Transform3D boneTrans = getBoneTransform(i, false);
            if (boneTrans == null) continue;

            if (i == bonesInChain.length - 1) {
                if (bonesInChain.length > 2) {
                    Vector3 bOrigin = getNodeOrigin(boneNodes.get(i));
                    Vector3 bOriginLocal = toSkeletonSpace(bOrigin, skelOrigin);
                    Vector3 bPrevOrigin = getNodeOrigin(boneNodes.get(i - 1));
                    Vector3 bPrevOriginLocal = toSkeletonSpace(bPrevOrigin, skelOrigin);

                    Vector3 targetGlobalPos = Vector3.ZERO;
                    if (targetNode != null) {
                        targetGlobalPos = (Vector3) targetNode.call("get_global_position");
                        if (targetGlobalPos == null) targetGlobalPos = Vector3.ZERO;
                    }
                    Vector3 d = targetGlobalPos.sub(bPrevOriginLocal).normalized();

                    Vector3 lookTarget = bOriginLocal.add(d);
                    Basis lookBasis = makeLookAtBasis(lookTarget.sub(boneTrans.getOrigin()), Vector3.UP);
                    Transform3D newTrans = new Transform3D(lookBasis, bOriginLocal);
                    setBoneTransform(i, newTrans);
                } else {
                    Vector3 targetGlobalPos = Vector3.ZERO;
                    if (targetNode != null) {
                        targetGlobalPos = (Vector3) targetNode.call("get_global_position");
                        if (targetGlobalPos == null) targetGlobalPos = Vector3.ZERO;
                    }
                    Vector3 targetLocal = toSkeletonSpace(targetGlobalPos, skelOrigin);
                    Basis lookBasis2 = makeLookAtBasis(targetLocal.sub(boneTrans.getOrigin()), Vector3.UP);
                    Transform3D newTrans = new Transform3D(lookBasis2, boneTrans.getOrigin());

                    Object prevTfObj = boneNodes.get(i - 1).call("get_global_transform");
                    if (prevTfObj instanceof Transform3D) {
                        Transform3D prevTf = (Transform3D) prevTfObj;
                        Basis prevBasis = prevTf.getBasis();
                        Vector3 prevZ = new Vector3(prevBasis.zx, prevBasis.zy, prevBasis.zz).normalized();
                        Vector3 boneOrigin = prevTf.getOrigin().sub(prevZ.mul(bonesInChainLengths[i - 1]));
                        newTrans = new Transform3D(newTrans.getBasis(), toSkeletonSpace(boneOrigin, skelOrigin));
                    }
                    setBoneTransform(i, newTrans);
                }
            } else {
                Vector3 bOrigin = getNodeOrigin(boneNodes.get(i));
                Vector3 bOriginLocal = toSkeletonSpace(bOrigin, skelOrigin);
                Vector3 bNextOrigin = getNodeOrigin(boneNodes.get(i + 1));
                Vector3 bNextOriginLocal = toSkeletonSpace(bNextOrigin, skelOrigin);

                Vector3 d = bNextOriginLocal.sub(bOriginLocal).normalized();
                Vector3 lookTarget = bOriginLocal.add(d);
                Basis lookBasis3 = makeLookAtBasis(lookTarget.sub(boneTrans.getOrigin()), Vector3.UP);
                Transform3D newTrans = new Transform3D(lookBasis3, bOriginLocal);
                setBoneTransform(i, newTrans);
            }
        }
    }

    private Vector3 toSkeletonSpace(Vector3 worldPos, Vector3 skelOrigin) {
        Object skelGlobalObj = skeleton.getGlobalTransform();
        if (skelGlobalObj instanceof Transform3D) {
            Transform3D skelGlobal = (Transform3D) skelGlobalObj;
            return skelGlobal.inverse().apply(worldPos);
        }
        return worldPos.sub(skelOrigin);
    }

    private Transform3D getBoneTransform(int boneIndex, boolean convertToWorldSpace) {
        Integer boneId = boneIDs.get(bonesInChain[boneIndex]);
        if (boneId == null) return null;

        Object retObj = skeleton.getBoneGlobalPose(boneId);
        if (!(retObj instanceof Transform3D)) return null;
        Transform3D ret = (Transform3D) retObj;

        if (convertToWorldSpace) {
            Object skelGlobalObj = skeleton.getGlobalTransform();
            if (skelGlobalObj instanceof Transform3D) {
                Transform3D skelGlobal = (Transform3D) skelGlobalObj;
                Vector3 worldOrigin = skelGlobal.apply(ret.getOrigin());
                ret = new Transform3D(ret.getBasis(), worldOrigin);
            }
        }
        return ret;
    }

    private void setBoneTransform(int boneIndex, Transform3D trans) {
        Integer boneId = boneIDs.get(bonesInChain[boneIndex]);
        if (boneId == null) return;
        skeleton.setBoneGlobalPoseOverride(boneId, trans, 1.0, true);
    }
}
