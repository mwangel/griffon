/*
 * Copyright 2008-2014 the original author or authors.
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
package editor

import griffon.core.artifact.GriffonView
import griffon.metadata.ArtifactProviderFor

import java.awt.BorderLayout

import static griffon.util.GriffonApplicationUtils.isMacOSX

@ArtifactProviderFor(GriffonView)
class ContainerView {
    def builder
    def model

    void initUI() {
        builder.with {
            actions {
                action(saveAction,
                    enabled: bind { model.documentModel.dirty })
            }

            fileChooser(id: 'fileChooserWindow')
            application(title: application.configuration['application.title'],
                size: [480, 320], locationByPlatform: true,
                iconImage: imageIcon('/griffon-icon-48x48.png').image,
                iconImages: [imageIcon('/griffon-icon-48x48.png').image,
                    imageIcon('/griffon-icon-32x32.png').image,
                    imageIcon('/griffon-icon-16x16.png').image]) {
                menuBar {
                    menu('File') {
                        menuItem(openAction)
                        menuItem(closeAction)
                        separator()
                        menuItem(saveAction)
                        if (!isMacOSX) {
                            separator()
                            menuItem(quitAction)
                        }
                    }
                }

                borderLayout()
                tabbedPane(id: 'tabGroup', constraints: BorderLayout.CENTER)
                noparent {
                    tabGroup.addChangeListener(model)
                }
            }
        }
    }
}
