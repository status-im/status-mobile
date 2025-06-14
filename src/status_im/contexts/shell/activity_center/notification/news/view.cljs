(ns status-im.contexts.shell.activity-center.notification.news.view
  (:require [clojure.string :as string]
            [promesa.core :as promesa]
            [quo.core :as quo]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]
            [react-native.gesture :as gesture]
            [status-im.contexts.shell.activity-center.notification.common.view :as common]
            [utils.datetime :as datetime]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn auto-resized-image
  [url]
  (let [[height set-height] (rn/use-state nil)
        window-width        (- (:width (rn/get-window)) 40)]
    (rn/use-effect #(-> (rn/image-get-size url)
                        (promesa/then (fn [[w h]]
                                        (let [scale      (/ window-width w)
                                              new-height (* h scale)]
                                          (set-height new-height))))))
    (if height
      [fast-image/fast-image
       {:source url
        :style  {:width         window-width
                 :height        height
                 :align-self    :center
                 :border-radius 12}}]
      [rn/view {:style {:height 200 :align-items :center :justify-content :center}}
       [rn/activity-indicator]])))

(defn news-sheet
  [{:keys [news-image-url news-title news-content news-link news-link-label]} timestamp]
  (let [customization-color (rf/sub [:profile/customization-color])]
    [:<>
     [quo/drawer-top {:title news-title :description timestamp}]
     [rn/scroll-view {:style {:flex 1}}
      (when (not (string/blank? news-image-url))
        [auto-resized-image news-image-url])
      [quo/text
       {:style {:padding-horizontal 20
                :padding-vertical   8}}
       news-content]]
     (when (and (not (string/blank? news-link)) (not (string/blank? news-link-label)))
       [quo/bottom-actions
        {:button-one-label news-link-label
         :button-one-props {:customization-color customization-color
                            :icon-right          :i/external
                            :on-press            (fn []
                                                   (rf/dispatch [:browser.ui/open-url news-link])
                                                   (rf/dispatch [:hide-bottom-sheet]))}}])]))

(defn view
  [{:keys [notification extra-fn]}]
  (let [customization-color (rf/sub [:profile/customization-color])
        {:keys [news-title
                news-description
                timestamp
                read
                id]}        notification
        timestamp           (datetime/timestamp->relative timestamp)
        show-bottom-preview (rn/use-callback
                             (fn []
                               (rf/dispatch [:activity-center.notifications/mark-as-read id])
                               (rf/dispatch [:show-bottom-sheet
                                             {:theme   :dark
                                              :content (fn []
                                                         [news-sheet notification timestamp])}])))]
    [common/swipeable
     {:left-button    common/swipe-button-read-or-unread
      :left-on-press  common/swipe-on-press-toggle-read
      :right-button   common/swipe-button-delete
      :right-on-press common/swipe-on-press-delete
      :extra-fn       extra-fn}
     [gesture/touchable-without-feedback
      {:on-press show-bottom-preview}
      [quo/activity-log
       {:title               news-title
        :customization-color customization-color
        :icon                :i/status-logo-bw
        :timestamp           timestamp
        :unread?             (not read)
        :context             [[quo/text {} news-description]]
        :items               [{:type                :button
                               :subtype             :primary
                               :key                 :button-reply
                               :customization-color customization-color
                               :label               (i18n/label :t/read-more)
                               :accessibility-label :read-more
                               :on-press            show-bottom-preview}]}]]]))
