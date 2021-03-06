/*
 * Copyright 2006-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.mail.model;

import com.consol.citrus.CitrusConstants;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Body part representation holds content as String and optional attachment parts.
 * @author Christoph Deppisch
 * @since 1.4
 */
public class BodyPart {

    private String contentType;

    private String content;
    private List<AttachmentPart> attachments;

    /**
     * Default constructor using content and contentType.
     * @param content
     * @param contentType
     */
    public BodyPart(String content, String contentType) {
        this.content = content;
        this.contentType = contentType;
    }

    /**
     * Adds new attachment part.
     * @param part
     */
    public void addPart(AttachmentPart part) {
        if (attachments == null) {
            attachments = new ArrayList<AttachmentPart>();
        }

        this.attachments.add(part);
    }

    /**
     * Evaluates charset name from content type string. Or returns default charset name
     * when no charset is defined in content type string.
     * @return charset name
     */
    public String getCharsetName() {
        if (StringUtils.hasText(contentType) && contentType.contains("charset=")) {
            String charsetName = contentType.substring(contentType.indexOf("charset=") + "charset=".length());

            if (charsetName.contains(";")) {
                return charsetName.substring(0, charsetName.indexOf(';'));
            } else {
                return charsetName;
            }
        } else {
            return System.getProperty(CitrusConstants.CITRUS_FILE_ENCODING, "UTF-8");
        }
    }

    /**
     * Gets the content type.
     * @return
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the content type.
     * @param contentType
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Gets the content as string.
     * @return
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the content as string.
     * @param content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets the attachent list.
     * @return
     */
    public List<AttachmentPart> getAttachments() {
        return attachments;
    }

    /**
     * Sets the attachment list.
     * @param attachments
     */
    public void setAttachments(List<AttachmentPart> attachments) {
        this.attachments = attachments;
    }

    /**
     * Checks if attachments are present.
     * @return
     */
    public boolean hasAttachments() {
        return !CollectionUtils.isEmpty(attachments);
    }
}
