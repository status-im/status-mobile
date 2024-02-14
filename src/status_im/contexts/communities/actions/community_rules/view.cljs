(ns status-im.contexts.communities.actions.community-rules.view
  (:require
    [quo.components.buttons.slide-button.utils :as slide-button-utils]
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [status-im.contexts.communities.actions.community-rules.style :as style]
    [utils.re-frame :as rf]))

(defn view
  [id]
  (let [rules                 (rf/sub [:communities/rules id])
        android-bottom-margin (if platform/android? 44 0)
        rules-bottom-space    (+ (slide-button-utils/get-dimensions nil :size-48 :track-height)
                                 android-bottom-margin)]
    [rn/view {:style (style/community-rule rules-bottom-space)}
     [quo/text
      {:weight :regular
       :size   :paragraph-2}
      rules]]))
