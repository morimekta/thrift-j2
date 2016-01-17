/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package net.morimekta.providence.descriptor;

/**
 * Field requirement designation.
 *  - Default: Value is always present unless nullable. Not required for validity.
 *  - Optional: Value may always be missing.
 *  - Required: Value is always present unless nullable. Value required for validity.
 */
public enum PRequirement {
    DEFAULT(true, false, ""),
    OPTIONAL(false, false, "optional"),
    REQUIRED(false, true, "required");

    public boolean fieldIsValueType;
    public boolean presenceRequired;
    public String label;

    PRequirement(boolean value, boolean presence, String name) {
        fieldIsValueType = value;
        presenceRequired = presence;
        label = name;
    }

    public static PRequirement forName(String label) {
        switch (label) {
            case "REQUIRED":
                return REQUIRED;
            case "OPTIONAL":
                return OPTIONAL;
            default:
                return DEFAULT;
        }
    }

    public static PRequirement forLabel(String label) {
        switch (label) {
            case "required":
                return REQUIRED;
            case "optional":
                return OPTIONAL;
            default:
                return DEFAULT;
        }
    }
}