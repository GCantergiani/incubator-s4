/*
 * Copyright (c) 2010 Yahoo! Inc. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 	        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License. See accompanying LICENSE file. 
 */
package org.apache.s4.ft;

/**
 * 
 * Callback for reporting the result of an asynchronous storage operation
 *
 */
public interface StorageCallback {
	
    /**
     * Notifies the result of a storage operation
     * 
     * @param resultCode code for the result : {@link SafeKeeper.StorageResultCode SafeKeeper.StorageResultCode}
     * @param message whatever message object is suitable
     */
	public void storageOperationResult(SafeKeeper.StorageResultCode resultCode,
			Object message);

}