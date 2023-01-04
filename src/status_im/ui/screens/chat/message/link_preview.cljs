(ns status-im.ui.screens.chat.message.link-preview
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [re-frame.core :as re-frame]
            [status-im.chat.models.link-preview :as link-preview]
            [status-im.constants :as constants]
            [i18n.i18n :as i18n]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.message.styles :as styles]
            [status-im.ui.screens.communities.icon :as communities.icon]
            [utils.security.core :as security])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn link-belongs-to-domain
  [link domain]
  (cond
    (string/starts-with? link (str "https://" domain))     true
    (string/starts-with? link (str "https://www." domain)) true
    :else                                                  false))

(defn community-id-from-link
  [link]
  (nth (re-find constants/regx-community-universal-link link) 4))

(defn domain-info-if-whitelisted
  [link whitelist]
  (first (filter
          #(link-belongs-to-domain link (:address %))
          whitelist)))

(defn link-extended-info
  [link whitelist enabled-list]
  (let [domain-info  (domain-info-if-whitelisted link whitelist)
        community-id (community-id-from-link link)]
    (if-not community-id
      {:whitelisted (not (nil? domain-info))
       :enabled     (contains? enabled-list (:title domain-info))
       :link        link}
      {:whitelisted true
       :enabled     true
       :link        link
       :community   true})))

(defn previewable-link
  [links whitelist enabled-list]
  (->> links
       (map #(link-extended-info % whitelist enabled-list))
       (filter #(:whitelisted %))
       (first)))

(defview link-preview-enable-request
  []
  [react/view (styles/link-preview-request-wrapper)
   [react/view {:margin 12}
    [react/image
     {:source (resources/get-theme-image :unfurl)
      :style  styles/link-preview-request-image}]
    [quo/text
     {:size  :small
      :align :center
      :style {:margin-top 6}}
     (i18n/label :t/enable-link-previews)]
    [quo/text
     {:size  :small
      :color :secondary
      :align :center
      :style {:margin-top 2}}
     (i18n/label :t/once-enabled-share-metadata)]]
   [quo/separator]
   [quo/button
    {:on-press #(re-frame/dispatch [:open-modal :link-preview-settings])
     :type     :secondary}
    (i18n/label :enable)]
   [quo/separator]
   [quo/button
    {:on-press #(re-frame/dispatch
                 [::link-preview/should-suggest-link-preview false])
     :type     :secondary}
    (i18n/label :t/dont-ask)]])

(defn is-gif?
  [url]
  (string/ends-with? url ".gif"))

(defview link-preview-loader
  [link outgoing timeline]
  (letsubs [cache [:link-preview/cache]]
    (let [{:keys [site title thumbnailUrl error] :as preview-data} (get cache link)]
      (if (not preview-data)
        (do
          (re-frame/dispatch
           [::link-preview/load-link-preview-data link])
          nil)
        (when-not error
          [react/touchable-highlight
           {:style    (when-not (is-gif? thumbnailUrl) {:align-self :stretch})
            :on-press #(when (security/safe-link? link)
                         (re-frame/dispatch
                          [:browser.ui/message-link-pressed link]))}
           [react/view (styles/link-preview-wrapper outgoing timeline)
            (when-not (string/blank? thumbnailUrl)
              [react/image
               {:source              {:uri thumbnailUrl}
                :style               (styles/link-preview-image outgoing
                                                                (select-keys preview-data
                                                                             [:height :width]))
                :accessibility-label :member-photo}])
            (when-not (is-gif? thumbnailUrl)
              [:<>
               [quo/text
                {:size  :small
                 :style styles/link-preview-title}
                title]
               [quo/text
                {:size  :small
                 :color :secondary
                 :style styles/link-preview-site}
                site]])]])))))

(defview community-preview
  [community outgoing timeline]
  (let [{:keys [name members description verified]} community
        members-count                               (count members)]
    [react/view (styles/link-preview-wrapper outgoing timeline)
     (if verified
       [quo/text
        {:size  :small
         :color :link
         :style styles/community-preview-header}
        (i18n/label :t/verified-community)]
       [quo/text
        {:size  :small
         :color :secondary
         :style styles/community-preview-header}
        (i18n/label :t/community)])
     [quo/separator]
     [react/view {:flex-direction :row :align-self :flex-start :margin 12}
      [communities.icon/community-icon community]
      [react/view {:flex 1 :flex-direction :column :margin-left 12}
       [quo/text {:weight :bold :size :large} name]
       [quo/text description]
       [quo/text
        {:size  :small
         :color :secondary}
        (i18n/label-pluralize members-count :t/community-members {:count members-count})]]]
     [quo/separator]
     [quo/button
      {:on-press #(re-frame/dispatch [:navigate-to
                                      :community
                                      {:from-chat    true
                                       :community-id (:id community)}])
       :type     :secondary}
      (i18n/label :t/view)]]))

(defview community-preview-loader
  [community-link outgoing timeline]
  (letsubs [cache [:link-preview/cache]]
    {:component-did-mount (fn []
                            (let [community    (get cache community-link)
                                  community-id (community-id-from-link community-link)]
                              (when-not community
                                (re-frame/dispatch
                                 [::link-preview/resolve-community-info community-id]))))}
    (when-let [community (get cache community-link)]
      [community-preview community outgoing timeline])))

(defview link-preview-wrapper
  [links outgoing timeline]
  (letsubs
    [ask-user?     [:link-preview/link-preview-request-enabled]
     whitelist     [:link-previews-whitelist]
     enabled-sites [:link-preview/enabled-sites]]
    (when links
      (let [link-info                                    (previewable-link links whitelist enabled-sites)
            {:keys [link whitelisted enabled community]} link-info
            link-whitelisted                             (and link whitelisted)]
        (cond
          community                        [community-preview-loader link outgoing timeline]
          (and link-whitelisted enabled)   [link-preview-loader link outgoing timeline]
          (and link-whitelisted ask-user?) [link-preview-enable-request])))))
