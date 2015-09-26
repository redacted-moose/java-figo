//
// Copyright (c) 2013 figo GmbH
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
//

package me.figo;

import com.android.volley.VolleyError;

/***
 * Base Class for all figo Exceptions. It extends the normal Java exceptions with an error_code field, which carries the computer readable error reason.
 * 
 * @author Stefan Richter
 */
public class FigoError extends VolleyError {

    private static final long serialVersionUID = -3645017096212930985L;

    private final String error_code;

    public FigoError(String error_code, String error_message) {
        super(error_message);

        this.error_code = error_code;
    }

    public FigoError(String error_code, String error_message, Throwable exc) {
        super(error_message, exc);

        this.error_code = error_code;
    }

    public FigoError(ErrorResponse response) {
        this(response.getError(), response.getErrorDescription());
    }

    public String getErrorCode() {
        return error_code;
    }

    public static class ErrorResponse {
        private String error;
        private String error_description;

        public ErrorResponse() {
        }

        public String getError() {
            return error;
        }

        public String getErrorDescription() {
            return error_description;
        }
    }
}
