(ns status-im.ui.components.contact.contact
  (:require [clojure.string :as string]
            [status-im.i18n :as i18n]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.ui.components.contact.styles :as styles]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ethereum.stateofus :as stateofus]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.platform :as platform])
  (:require-macros [status-im.utils.views :as views]))

(defn desktop-extended-options [options]
  [react/view {}
   (doall (for [{:keys [label action]} options]
            ^{:key label}
            [react/touchable-highlight
             {:on-press action}
             [react/view {}
              [react/text label]]]))])

(defn format-name [{:keys [ens-verified name public-key]}]
  (if ens-verified
    (str "@" (or (stateofus/username name) name))
    (gfycat/generate-gfy public-key)))
