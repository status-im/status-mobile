(ns status-im.ui.screens.chat.message.link-preview
  (:require [re-frame.core :as re-frame]
            [clojure.string :as string]
            [status-im.ui.components.react :as react]
            [quo.core :as quo]
            [status-im.utils.security :as security]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.screens.chat.message.styles :as styles]
            [status-im.react-native.resources :as resources]
            [status-im.chat.models.link-preview :as link-preview])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn link-belongs-to-domain [link domain]
  (cond
    (string/starts-with? link (str "https://" domain)) true
    (string/starts-with? link (str "https://www." domain)) true
    :else false))

(defn domain-info-if-whitelisted [link whitelist]
  (first (filter
          #(link-belongs-to-domain link (:address %))
          whitelist)))

(defn link-extended-info [link whitelist enabled-list]
  (let [domain-info (domain-info-if-whitelisted link whitelist)]
    {:whitelisted (not (nil? domain-info))
     :enabled (contains? enabled-list (:title domain-info))
     :link link}))

(defn previewable-link [links whitelist enabled-list]
  (->> links
       (map #(link-extended-info % whitelist enabled-list))
       (filter #(:whitelisted %))
       (first)))

(defview link-preview-enable-request []
  [react/view (styles/link-preview-request-wrapper)
   [react/view {:margin 12}
    [react/image {:source (resources/get-theme-image :unfurl)
                  :style  styles/link-preview-request-image}]
    [quo/text {:size :small
               :align :center
               :style {:margin-top 6}}
     (i18n/label :t/enable-link-previews)]
    [quo/text {:size :small
               :color :secondary
               :align :center
               :style {:margin-top 2}}
     (i18n/label :t/once-enabled-share-metadata)]]
   [quo/separator]
   [quo/button {:on-press #(re-frame/dispatch [:navigate-to :link-preview-settings])
                :type     :secondary}
    (i18n/label :enable)]
   [quo/separator]
   [quo/button {:on-press #(re-frame/dispatch
                            [::link-preview/should-suggest-link-preview false])
                :type     :secondary}
    (i18n/label :t/dont-ask)]])

(defn is-gif? [url]
  (string/ends-with? url ".gif"))

(defview link-preview-loader [link outgoing timeline]
  (letsubs [cache [:link-preview/cache]]
    (let [{:keys [site title thumbnailUrl error] :as preview-data} (get cache link)]
      (if (not preview-data)
        (do
          (re-frame/dispatch
           [::link-preview/load-link-preview-data link])
          nil)
        (when-not error
          [react/touchable-highlight
           {:style (when-not (is-gif? thumbnailUrl) {:align-self :stretch})
            :on-press #(when (and (security/safe-link? link))
                         (re-frame/dispatch
                          [:browser.ui/message-link-pressed link]))}
           [react/view (styles/link-preview-wrapper outgoing timeline)
            [react/image {:source              {:uri thumbnailUrl}
                          :style               (styles/link-preview-image outgoing (select-keys preview-data [:height :width]))
                          :accessibility-label :member-photo}]
            (when-not (is-gif? thumbnailUrl)
              [:<>
               [quo/text {:size  :small
                          :style styles/link-preview-title}
                title]
               [quo/text {:size  :small
                          :color :secondary
                          :style styles/link-preview-site}
                site]])]])))))

(defview link-preview-wrapper [links outgoing timeline]
  (letsubs
    [ask-user? [:link-preview/link-preview-request-enabled]
     whitelist [:link-preview/whitelist]
     enabled-sites   [:link-preview/enabled-sites]]
    (when links
      (let [link-info (previewable-link links whitelist enabled-sites)
            {:keys [link whitelisted enabled]} link-info]
        (when (and link whitelisted)
          (if enabled
            [link-preview-loader link outgoing timeline]
            (when ask-user?
              [link-preview-enable-request])))))))
