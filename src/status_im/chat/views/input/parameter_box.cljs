(ns status-im.chat.views.input.parameter-box
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                                scroll-view
                                                touchable-highlight
                                                text
                                                icon]]
            [status-im.chat.styles.input.parameter-box :as style]
            [status-im.chat.views.input.utils :as input-utils]
            [status-im.i18n :refer [label]]
            [taoensso.timbre :as log]
            [clojure.string :as str]))

(defn header []
  [view {:style style/header-container}
   [view style/header-icon]])

(defview parameter-box-container []
  [parameter-box [:chat-parameter-box]]
  (when (:hiccup parameter-box)
    (:hiccup parameter-box)))

(defview parameter-box-view []
  [input-height [:chat-ui-props :input-height]
   layout-height [:get :layout-height]
   chat-input-margin [:chat-input-margin]
   chat-parameter-box [:chat-parameter-box]
   input-text [:chat :input-text]
   validation-messages [:chat-ui-props :validation-messages]]
  (when (and chat-parameter-box
             (not (str/blank? input-text))
             (not validation-messages))
    (let [bottom (+ input-height chat-input-margin)]
      [view (style/root (input-utils/max-area-height bottom layout-height)
                        bottom)
       [header]
       [parameter-box-container]])))