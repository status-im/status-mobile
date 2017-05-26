(ns status-im.network-settings.screens.paste-json-text
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require
    [re-frame.core :refer [dispatch]]
    [status-im.components.status-bar :refer [status-bar]]
    [status-im.components.toolbar-new.view :refer [toolbar]]
    [status-im.network-settings.screen :refer [network-badge]]
    [status-im.components.react :refer [view text text-input icon]]
    [status-im.components.sticky-button :refer [sticky-button]]
    [status-im.network-settings.styles :as st]
    [status-im.i18n :as i18n]
    [clojure.string :as str]))

(defview paste-json-text []
  (let [network-json "test"
        error nil]
    [view {:flex 1}
     [status-bar]
     [toolbar {:title (i18n/label :t/add-network)}]
     [network-badge]
     [view {:margin-top 16
            :margin-left 16
            :flex 1}
      (when error
        [text {:style {:color :red}}
         (i18n/label :t/error-processing-json)])
      [text-input {:style st/paste-json-text-input
                   :flex 1
                   :placeholder (i18n/label :t/paste-json)
                   :multiline true}]]
     (when (not (str/blank? network-json))
       [sticky-button (i18n/label :t/process-json) #()])]))
