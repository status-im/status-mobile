(ns status-im2.contexts.chat.messages.link-preview.view
  (:require
   [clojure.string :as string]
   [i18n.i18n :as i18n]
   [reagent.core :as reagent]
   [react-native.core :as rn]
   [status-im.constants :as constants]
   [status-im2.contexts.chat.messages.link-preview.events]
   [status-im2.contexts.chat.messages.link-preview.style :as style]
   [quo2.core :as quo]
   [utils.re-frame :as rf]
   [utils.security.core :as security]))

(defn link-belongs-to-domain
  [link domain]
  (string/starts-with? link (str "https://" domain)))

(defn community-id-from-link
  [link]
  (nth (re-find constants/regx-community-universal-link link) 4))

(defn domain-info-if-whitelisted
  [link whitelist]
  (->> whitelist
       (filter #(link-belongs-to-domain link (:address %)))
       first))

(defn link-extended-info
  [link whitelist enabled-list]
  (if-not (community-id-from-link link)
    (let [domain-info (domain-info-if-whitelisted link whitelist)]
      {:whitelisted? (boolean domain-info)
       :enabled?     (contains? enabled-list (:title domain-info))
       :link         link})
    {:whitelisted? true
     :enabled?     true
     :link         link
     :community?   true}))

(defn previewable-link
  [links whitelist enabled-list]
  (->> links
       (map #(link-extended-info % whitelist enabled-list))
       (filter #(:whitelisted? %))
       (first)))

(defn is-gif?
  [url]
  (and url (string/ends-with? url ".gif")))

(defn community-preview
  [{:keys [name members description verified] :as community}]
  (let [members-count (count members)]
    [rn/view (style/wrapper)
     [rn/view (style/title-wrapper)
      [rn/image {:style (style/title-site-image)}]
      [rn/text {:style (style/title-text)}
       name]]
     [rn/text {:style (style/extra-text)}
      (if verified
        (i18n/label :t/verified-community)
        (i18n/label :t/community))]
     [rn/text {:style (style/main-text)}
      description]
     [rn/text {:style (style/extra-text)}
      (i18n/label-pluralize members-count :t/community-members {:count members-count})]
     [rn/view (style/separator)]
     [quo/button
      {:type     :grey
       :on-press #(rf/dispatch [:navigate-to :community
                                {:from-chat    true
                                 :community-id (:id community)}])}
      (i18n/label :view)]]))

(defn community-preview-loader
  [community-link]
  (let [cached-preview-data (rf/sub [:link-preview/cache community-link])]
    (reagent/create-class
     {:component-did-mount
      (fn []
        (when-not cached-preview-data
          (let [community-id (community-id-from-link community-link)]
            (rf/dispatch [:chat.ui/resolve-community-info community-id]))))
      :reagent-render
      (fn []
        (when cached-preview-data
          [community-preview cached-preview-data]))})))

(defn link-preview-loader
  [link]
  (reagent/create-class
   {:component-did-mount
    (fn []
      (rf/dispatch [:chat.ui/load-link-preview-data link]))
    :reagent-render
    (fn []
      (let [cached-preview-data (rf/sub [:link-preview/cache link])]
        (when-let [{:keys [site title thumbnail-url error] :as preview-data} cached-preview-data]
          (when (and (not error) site title)
            [rn/touchable-opacity
             {:style    (when-not (is-gif? thumbnail-url)
                          {:align-self :stretch})
              :on-press #(when (security/safe-link? link)
                           (rf/dispatch [:browser.ui/message-link-pressed link]))}
             [rn/view (style/wrapper)
              (when-not (is-gif? thumbnail-url)
                [:<>
                 [rn/view (style/title-wrapper)
                  [rn/image {:style (style/title-site-image)}]
                  [rn/text {:style (style/title-text)}
                   site]]
                 [rn/text {:style (style/main-text)}
                  title]
                 [rn/text {:style (style/extra-text)}
                  link]])
              (when-not (string/blank? thumbnail-url)
                [:<>
                 [rn/view (style/separator)]
                 [rn/image
                  {:source              {:uri thumbnail-url}
                   :style               (style/image (select-keys preview-data [:height :width]))
                   :accessibility-label :member-photo}]])]]))))}))

(defn link-preview-enable-request
  []
  [rn/view (style/link-preview-enable-request-wrapper)
   [rn/text {:style (style/title-text)}
    (i18n/label :t/enable-link-previews)]
   [rn/text {:style (style/main-text)}
    (i18n/label :t/once-enabled-share-metadata)]
   [rn/view (style/separator)]
   [quo/button
    {:type     :grey
     :on-press #(rf/dispatch [:open-modal :link-preview-settings])}
    (i18n/label :enable)]
   [rn/view (style/separator)]
   [quo/button
    {:type     :grey
     :on-press #(rf/dispatch [:chat.ui/should-suggest-link-preview false])}
    (i18n/label :t/dont-ask)]])

(defn link-preview
  [{:keys [content]}]
  (let [links         (:links content)
        ask-user?     (rf/sub [:link-preview/link-preview-request-enabled])
        enabled-sites (rf/sub [:link-preview/enabled-sites])
        whitelist     (rf/sub [:link-previews-whitelist])]
    (when links
      (let [{:keys [link
                    whitelisted?
                    enabled?
                    community?]}
            (previewable-link links whitelist enabled-sites)
            link-whitelisted? (and link whitelisted?)]
        (cond
          community?                        [community-preview-loader link]
          (and link-whitelisted? enabled?)  [link-preview-loader link]
          (and link-whitelisted? ask-user?) [link-preview-enable-request])))))