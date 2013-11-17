/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.jobSystem;

import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.behavior.tree.Node;
import org.terasology.logic.behavior.tree.Status;
import org.terasology.logic.behavior.tree.Task;
import org.terasology.pathfinding.model.WalkableBlock;

import java.util.List;

/**
 * @author synopia
 */
public class FinishJobNode extends Node {
    @Override
    public Task create() {
        return new FinishJobTask(this);
    }

    public static class FinishJobTask extends Task {
        public FinishJobTask(Node node) {
            super(node);
        }

        @Override
        public Status update(float dt) {
            JobMinionComponent actorJob = actor().job();
            EntityRef currentJob = actorJob.currentJob;
            if (currentJob != null) {
                List<WalkableBlock> targetPositions = currentJob.getComponent(JobBlockComponent.class).getJob().getTargetPositions(currentJob);
                WalkableBlock currentBlock = actor().move().currentBlock;
                if (!targetPositions.contains(currentBlock)) {
                    return Status.RUNNING;
                }
                CoreRegistry.get(JobBoard.class).removeJob(currentJob);
                actorJob.currentJob = null;
                actor().save(actorJob);
            }
            return Status.SUCCESS;
        }
    }
}
