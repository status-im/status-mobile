(ns status-im.contexts.profile.edit.list-items
  (:require [quo.foundations.colors :as colors]
            [status-im.common.not-implemented :as not-implemented]
            [status-im.contexts.profile.edit.style :as style]
            [status-im.contexts.profile.utils :as profile.utils]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn items
  []
  (let [profile             (rf/sub [:profile/profile-with-image])
        customization-color (rf/sub [:profile/customization-color])
        full-name           (profile.utils/displayed-name profile)]
    [{:label (i18n/label :t/profile)
      :items [{:title           (i18n/label :t/name)
               :on-press        #(rf/dispatch [:open-modal :edit-name])
               :blur?           true
               :label           :text
               :label-props     full-name
               :action          :arrow
               :container-style style/item-container}
              {:title           (i18n/label :t/bio)
               :on-press        not-implemented/alert
               :blur?           true
               :action          :arrow
               :container-style style/item-container}
              {:title           (i18n/label :t/accent-colour)
               :on-press        not-implemented/alert
               :label           :color
               :label-props     (colors/resolve-color customization-color :dark)
               :blur?           true
               :action          :arrow
               :container-style style/item-container}]}

     {:label (i18n/label :t/showcase)
      :items [{:title           (i18n/label :t/communities)
               :on-press        not-implemented/alert
               :blur?           true
               :action          :arrow
               :container-style style/item-container}
              {:title           (i18n/label :t/accounts)
               :on-press        not-implemented/alert
               :blur?           true
               :action          :arrow
               :container-style style/item-container}
              {:title           (i18n/label :t/collectibles)
               :on-press        not-implemented/alert
               :blur?           true
               :action          :arrow
               :container-style style/item-container}
              {:title           (i18n/label :t/assets)
               :on-press        not-implemented/alert
               :blur?           true
               :action          :arrow
               :container-style style/item-container}]}

     {:label (i18n/label :t/on-the-web)
      :items [{:title           (i18n/label :t/links)
               :on-press        not-implemented/alert
               :blur?           true
               :action          :arrow
               :container-style style/item-container}]}]))
