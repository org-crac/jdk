/*
 * Copyright (c) 2018, 2020, Azul Systems, Inc. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Azul Systems, 385 Moffett Park Drive, Suite 115, Sunnyvale,
 * CA 94089 USA or visit www.azul.com if you need additional information or
 * have any questions.
 */

package javax.crac;

/**
 * A group of {@code Resource}s. Distributes notifications between the
 * group.
 */
public abstract class Context<R extends Resource> implements Resource {
    @Override
    public void beforeCheckpoint() throws CheckpointException {
        throw new RuntimeException("unimplemented");
    }

    @Override
    public void afterRestore() throws RestoreException {
        throw new RuntimeException("unimplemented");
    }

    /**
     * Registers a {@code Resource} in this {@code Context}. The {@code Context}
     * will maintain weak reference on the {@code Resource}.
     *
     * @param resource {@code Resource} to be registered.
     */
    void register(R resource) {
        throw new RuntimeException("unimplemented");
    }
}
