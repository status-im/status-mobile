(ns status-im.contexts.profile.edit.name.view
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [reagent.core :as reagent]
            [status-im.constants :as constants]
            [status-im.contexts.profile.edit.name.style :as style]
            [status-im.contexts.profile.utils :as profile.utils]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  []
  (let [insets              (safe-area/get-insets)
        profile             (rf/sub [:profile/profile-with-image])
        customization-color (rf/sub [:profile/customization-color])
        display-name        (profile.utils/displayed-name profile)
        full-name           (reagent/atom display-name)]
    [quo/overlay {:type :shell}
     [rn/view
      {:key   :header
       :style (style/page-wrapper insets)}
      [quo/page-nav
       {:background :blur
        :icon-name  :i/arrow-left
        :on-press   #(rf/dispatch [:navigate-back])}]
      [rn/view {:style style/screen-container}
       [rn/view {:style {:gap 20}}
        [quo/text-combinations {:title (i18n/label :t/name)}]
        [quo/input
         {:default-value  @full-name
          :auto-focus     true
          :char-limit     constants/profile-name-max-length
          :label          (i18n/label :t/name)
          :on-change-text #(reset! full-name (string/trim %))}]]
       [quo/button
        {:accessibility-label :submit-create-profile-button
         :type                :primary
         :customization-color customization-color
         :on-press            (fn []
                                (rf/dispatch [:onboarding/profile-data-set
                                              {:display-name @full-name}]))
         :disabled?           (not (seq @full-name))}
        (i18n/label :t/save-name)]]]]))
