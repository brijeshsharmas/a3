/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.service;

import a3.model.Envelope;

/**
 * @author Brijesh Sharma
 *
 */
public interface A3EmailService extends A3Service {
	
	public Envelope send(Envelope email);

}
