package demos.threed.ik;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.godot.annotation.GodotClass;
import org.godot.math.Basis;
import org.godot.math.Transform3D;
import org.godot.math.Vector3;
import org.godot.node.Node;
import org.godot.node.Node3D;
import org.godot.node.Skeleton3D;

@GodotClass(name = "IKFabrik", parent = "Node3D")
public class IKFabrik extends Node3D {

    private static final double CHAIN_TOLERANCE = 0.01;
    private static final int CHAIN_MAX_ITER = 10;

    private Skeleton3D skeleton;
    private Node3D targetNode;
    private Node3D middleJointTarget;

    private String[] bonesInChain = new String[0];
    private double[] bonesInChainLengths = new double[0];
    private final Map<String, Integer> boneIDs = new HashMap<>();
    private final ArrayList<Node3D> boneNodes = new ArrayList<>();

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
        if (bonesObj instanceof String[] arr) bonesInChain = arr;
        else if (bonesObj instanceof Object[] arr) {
            bonesInChain = new String[arr.length];
            for (int i = 0; i < arr.length; i++) bonesInChain[i] = arr[i].toString();
        }

        Object lengthsObj = getProperty("bones_in_chain_lengths");
        if (lengthsObj instanceof double[] arr) bonesInChainLengths = arr;
        else if (lengthsObj instanceof float[] arr) {
            bonesInChainLengths = new double[arr.length];
            for (int i = 0; i < arr.length; i++) bonesInChainLengths[i] = arr[i];
        } else if (lengthsObj instanceof Number[] arr) {
            bonesInChainLengths = new double[arr.length];
            for (int i = 0; i < arr.length; i++) bonesInChainLengths[i] = arr[i].doubleValue();
        }

        Object limitObj = getProperty("limit_chain_iterations");
        if (limitObj instanceof Boolean value) limitChainIterations = value;

        Object resetObj = getProperty("reset_iterations_on_update");
        if (resetObj instanceof Boolean value) resetIterationsOnUpdate = value;

        Object midJointObj = getProperty("use_middle_joint_target");
        if (midJointObj instanceof Boolean value) useMiddleJointTarget = value;

        Object umObj = getProperty("update_mode");
        if (umObj instanceof Number value) updateMode = value.intValue();

        setupSkeletonPath();
        targetNode = ensureNode3D("Target");
        middleJointTarget = ensureNode3D("MiddleJoint");
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

        Node temp = getNode(String.valueOf(skelPathObj));
        if (temp instanceof Skeleton3D skeletonNode) {
            skeleton = skeletonNode;
            boneIDs.clear();
            makeBoneNodes();
        }
    }

    private Node3D ensureNode3D(String name) {
        if (hasNode(name)) {
            Node node = getNode(name);
            if (node instanceof Node3D node3D) return node3D;
        }

        Node3D node = Node3D.create();
        node.setName(name);
        addChild(node);
        return node;
    }

    private void makeBoneNodes() {
        boneNodes.clear();
        for (String boneName : bonesInChain) {
            boneNodes.add(ensureNode3D(boneName));
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
                boneIDs.put(bonesInChain[i], skeleton.findBone(bonesInChain[i]));
                if (i < boneNodes.size()) {
                    Transform3D bt = getBoneTransform(i, true);
                    if (bt != null) {
                        boneNodes.get(i).setGlobalTransform(bt);
                    }
                    if (i < boneNodes.size() - 1) {
                        Transform3D nextBt = getBoneTransform(i + 1, true);
                        if (nextBt != null) {
                            Vector3 lookTarget = nextBt.getOrigin().add(skeleton.getGlobalPosition());
                            boneNodes.get(i).lookAt(lookTarget, Vector3.UP);
                        }
                    }
                }
            }
        }

        if (totalLength == Double.POSITIVE_INFINITY) {
            totalLength = 0;
            for (double length : bonesInChainLengths) totalLength += length;
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
            Transform3D prevTf = boneNodes.get(boneNodes.size() - 2).getGlobalTransform();
            Basis b = prevTf.getBasis();
            dir = new Vector3(b.zx, b.zy, b.zz).normalized();
        } else if (targetNode != null) {
            Basis b = targetNode.getGlobalTransform().getBasis();
            dir = new Vector3(b.zx, b.zy, b.zz).mul(-1).normalized();
        } else {
            dir = Vector3.FORWARD;
        }

        Vector3 targetPos = targetNode != null ? targetNode.getGlobalPosition() : Vector3.ZERO;
        targetPos = targetPos.add(dir.mul(bonesInChainLengths[boneNodes.size() - 1]));

        if (useMiddleJointTarget && boneNodes.size() > 2 && middleJointTarget != null) {
            Vector3 midPos = middleJointTarget.getGlobalPosition();
            Node3D middleBone = boneNodes.get(boneNodes.size() / 2);
            Vector3 boneMidPos = middleBone.getGlobalPosition();
            Vector3 normalized = midPos.sub(boneMidPos).normalized();
            Transform3D midGlobal = middleBone.getGlobalTransform();
            Vector3 newOrigin = midGlobal.getOrigin().add(normalized);
            middleBone.setGlobalTransform(new Transform3D(midGlobal.getBasis(), newOrigin));
        }

        Node3D endEffector = boneNodes.get(boneNodes.size() - 1);
        Vector3 endEffectorPos = endEffector.getGlobalPosition();
        double dif = endEffectorPos.sub(targetPos).length();

        while (dif > CHAIN_TOLERANCE) {
            chainBackward(targetPos, dir);
            chainForward();
            chainApplyRotation();

            endEffectorPos = endEffector.getGlobalPosition();
            dif = endEffectorPos.sub(targetPos).length();

            chainIterations++;
            if (chainIterations >= CHAIN_MAX_ITER) break;
        }

        for (int i = 0; i < boneNodes.size(); i++) {
            Transform3D resetTf = getBoneTransform(i, true);
            if (resetTf != null) {
                boneNodes.get(i).setGlobalTransform(resetTf);
            }
        }
    }

    private Vector3 getNodeOrigin(Node3D node) {
        return node.getGlobalTransform().getOrigin();
    }

    private void setNodeOrigin(Node3D node, Vector3 origin) {
        Transform3D tf = node.getGlobalTransform();
        node.setGlobalTransform(new Transform3D(tf.getBasis(), origin));
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
        Vector3 skelOrigin = skeleton.getGlobalPosition();

        for (int i = 0; i < bonesInChain.length; i++) {
            Transform3D boneTrans = getBoneTransform(i, false);
            if (boneTrans == null) continue;

            if (i == bonesInChain.length - 1) {
                applyEndBoneRotation(i, boneTrans, skelOrigin);
            } else {
                Vector3 bOrigin = getNodeOrigin(boneNodes.get(i));
                Vector3 bOriginLocal = toSkeletonSpace(bOrigin, skelOrigin);
                Vector3 bNextOrigin = getNodeOrigin(boneNodes.get(i + 1));
                Vector3 bNextOriginLocal = toSkeletonSpace(bNextOrigin, skelOrigin);

                Vector3 d = bNextOriginLocal.sub(bOriginLocal).normalized();
                Vector3 lookTarget = bOriginLocal.add(d);
                Basis lookBasis = makeLookAtBasis(lookTarget.sub(boneTrans.getOrigin()), Vector3.UP);
                setBoneTransform(i, new Transform3D(lookBasis, bOriginLocal));
            }
        }
    }

    private void applyEndBoneRotation(int i, Transform3D boneTrans, Vector3 skelOrigin) {
        if (bonesInChain.length > 2) {
            Vector3 bOrigin = getNodeOrigin(boneNodes.get(i));
            Vector3 bOriginLocal = toSkeletonSpace(bOrigin, skelOrigin);
            Vector3 bPrevOrigin = getNodeOrigin(boneNodes.get(i - 1));
            Vector3 bPrevOriginLocal = toSkeletonSpace(bPrevOrigin, skelOrigin);

            Vector3 targetGlobalPos = targetNode != null ? targetNode.getGlobalPosition() : Vector3.ZERO;
            Vector3 d = targetGlobalPos.sub(bPrevOriginLocal).normalized();

            Vector3 lookTarget = bOriginLocal.add(d);
            Basis lookBasis = makeLookAtBasis(lookTarget.sub(boneTrans.getOrigin()), Vector3.UP);
            setBoneTransform(i, new Transform3D(lookBasis, bOriginLocal));
        } else {
            Vector3 targetGlobalPos = targetNode != null ? targetNode.getGlobalPosition() : Vector3.ZERO;
            Vector3 targetLocal = toSkeletonSpace(targetGlobalPos, skelOrigin);
            Basis lookBasis = makeLookAtBasis(targetLocal.sub(boneTrans.getOrigin()), Vector3.UP);
            Transform3D newTrans = new Transform3D(lookBasis, boneTrans.getOrigin());

            Transform3D prevTf = boneNodes.get(i - 1).getGlobalTransform();
            Basis prevBasis = prevTf.getBasis();
            Vector3 prevZ = new Vector3(prevBasis.zx, prevBasis.zy, prevBasis.zz).normalized();
            Vector3 boneOrigin = prevTf.getOrigin().sub(prevZ.mul(bonesInChainLengths[i - 1]));
            newTrans = new Transform3D(newTrans.getBasis(), toSkeletonSpace(boneOrigin, skelOrigin));
            setBoneTransform(i, newTrans);
        }
    }

    private Vector3 toSkeletonSpace(Vector3 worldPos, Vector3 skelOrigin) {
        Transform3D skelGlobal = skeleton.getGlobalTransform();
        return skelGlobal.inverse().apply(worldPos);
    }

    private Transform3D getBoneTransform(int boneIndex, boolean convertToWorldSpace) {
        Integer boneId = boneIDs.get(bonesInChain[boneIndex]);
        if (boneId == null) return null;

        Transform3D ret = skeleton.getBoneGlobalPose(boneId);

        if (convertToWorldSpace) {
            Transform3D skelGlobal = skeleton.getGlobalTransform();
            Vector3 worldOrigin = skelGlobal.apply(ret.getOrigin());
            ret = new Transform3D(ret.getBasis(), worldOrigin);
        }
        return ret;
    }

    private void setBoneTransform(int boneIndex, Transform3D trans) {
        Integer boneId = boneIDs.get(bonesInChain[boneIndex]);
        if (boneId == null) return;
        skeleton.setBoneGlobalPoseOverride(boneId, trans, 1.0, true);
    }
}
