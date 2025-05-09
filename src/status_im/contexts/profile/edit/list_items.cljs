(ns status-im.contexts.profile.edit.list-items
  (:require [legacy.status-im.utils.core :as utils]
            [quo.foundations.colors :as colors]
            [status-im.common.not-implemented :as not-implemented]
            [status-im.config :as config]
            [status-im.constants :as constants]
            [status-im.contexts.profile.edit.style :as style]
            [status-im.contexts.profile.utils :as profile.utils]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn items
  [theme]
  (let [{:keys [bio ens-name?] :as profile} (rf/sub [:profile/profile-with-image])
        customization-color                 (rf/sub [:profile/customization-color])
        full-name                           (profile.utils/displayed-name profile)]
    [{:label (i18n/label :t/profile)
      :items [(cond-> {:title           (i18n/label :t/name)
                       :blur?           true
                       :label           :text
                       :label-props     (utils/truncate-str full-name constants/profile-name-max-length)
                       :container-style style/item-container}
                (not ens-name?)
                (assoc :on-press #(rf/dispatch [:open-modal :screen/edit-name])
                       :action   :arrow))
              {:title           (i18n/label :t/bio)
               :on-press        #(rf/dispatch [:open-modal :screen/edit-bio])
               :blur?           true
               :label           :text
               :label-props     (utils/truncate-str bio 15)
               :action          :arrow
               :container-style style/item-container}
              {:title           (i18n/label :t/accent-colour)
               :on-press        #(rf/dispatch [:open-modal :screen/edit-accent-colour])
               :label           :color
               :label-props     (colors/resolve-color customization-color theme)
               :blur?           true
               :action          :arrow
               :container-style style/item-container}]}

     (when config/show-not-implemented-features?
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
                 :container-style style/item-container}]})

     (when config/show-not-implemented-features?
       {:label (i18n/label :t/on-the-web)
        :items [{:title           (i18n/label :t/links)
                 :on-press        not-implemented/alert
                 :blur?           true
                 :action          :arrow
                 :container-style style/item-container}]})]))
