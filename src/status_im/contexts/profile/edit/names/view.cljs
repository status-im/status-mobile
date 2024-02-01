(ns status-im.contexts.profile.edit.names.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.contexts.profile.edit.names.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- name-item
  [{:keys [name ens-name? selected?]}]
  [quo/settings-item
   {:title           name
    :image-props     (if ens-name? :i/chevron-right :i/edit)
    :image           :icon
    :blur?           true
    :image-right?    true
    :on-press        #(rf/dispatch [:open-modal (if ens-name? :edit-ens :edit-name) name])
    :action          :selector
    :action-props    {:type      :radio
                      :blur?     true
                      :checked?  selected?
                      :on-change (fn [checked?]
                                   (when checked?
                                     (rf/dispatch [:profile.settings/profile-update :preferred-name
                                                   name])))}
    :container-style style/item-container}])

(defn- header-view
  []
  [rn/view {:style style/header-wrapper}
   [quo/text-combinations {:title (i18n/label :t/name)}]])

(defn view
  []
  (let [insets             (safe-area/get-insets)
        profile-user-names (rf/sub [:profile/profile-user-names])]
    [quo/overlay
     {:type            :shell
      :container-style (style/page-wrapper insets)}
     [quo/page-nav
      {:background :blur
       :icon-name  :i/arrow-left
       :on-press   #(rf/dispatch [:navigate-back])}]
     [rn/view
      {:style style/screen-container}
      [rn/flat-list
       {:header                          [header-view]
        :data                            profile-user-names
        :key-fn                          :name
        :shows-vertical-scroll-indicator false
        :render-fn                       name-item}]]]))
