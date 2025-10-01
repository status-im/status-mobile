(ns status-im.contexts.chat.messenger.photo-selector.album-selector.view
  (:require
    [quo.context]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [status-im.contexts.chat.messenger.photo-selector.album-selector.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn render-album
  [{title :title size :count uri :uri} index _ {:keys [album? selected-album top]}]
  (let [selected? (= selected-album title)
        theme     (quo.context/use-theme)]
    [rn/touchable-opacity
     {:on-press            (fn []
                             (rf/dispatch [:chat.ui/camera-roll-select-album title])
                             (rf/dispatch [:photo-selector/get-photos-for-selected-album])
                             (reanimated/animate top (:height (rn/get-window)))
                             (js/setTimeout #(reset! album? false) 300))
      :style               (style/album-container selected?)
      :accessibility-label (str "album-" index)}
     [rn/image
      {:source {:uri uri}
       :style  style/cover}]
     [rn/view {:style {:margin-left 12}}
      [quo/text
       {:weight          :medium
        :ellipsize-mode  :tail
        :number-of-lines 1
        :style           {:margin-right 50}}
       title]
      [quo/text
       {:size  :paragraph-2
        :style {:color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)}}
       (when size
         (str size
              " "
              (if (= size 1)
                (i18n/label :t/image)
                (i18n/label :t/images))))]]
     (when selected?
       [rn/view
        {:style {:position :absolute
                 :right    16}}
        [quo/icon :i/check
         {:size 20 :color (colors/theme-colors colors/primary-50 colors/primary-60 theme)}]])]))

(def no-title "no-title")

(defn section-header
  [{:keys [title]}]
  (when-not (= title no-title)
    [quo/divider-label
     {:container-style style/divider
      :tight?          false}
     title]))

(defn key-fn
  [item index]
  (str (:title item) index))

(defn- f-album-selector
  [{:keys [scroll-enabled? on-scroll]} album? selected-album top]
  (let [theme                      (quo.context/use-theme)
        albums                     (rf/sub [:camera-roll/albums])
        total-photos-count-android (rf/sub [:camera-roll/total-photos-count-android])
        total-photos-count-ios     (rf/sub [:camera-roll/total-photos-count-ios])
        albums-sections            [{:title no-title
                                     :data  [(assoc (:smart-album albums)
                                                    :count
                                                    (if platform/ios?
                                                      total-photos-count-ios
                                                      total-photos-count-android))]}
                                    {:title (i18n/label :t/my-albums)
                                     :data  (:my-albums albums)}]
        window-height              (:height (rn/get-window))]
    [reanimated/view {:style (style/selector-container top theme)}
     [gesture/section-list
      {:data                           albums-sections
       :sections                       albums-sections
       :render-data                    {:album?         album?
                                        :selected-album selected-album
                                        :top            top}
       :render-fn                      render-album
       :sticky-section-headers-enabled false
       :render-section-header-fn       section-header
       :content-container-style        {:padding-top    64
                                        :padding-bottom 40}
       :key-fn                         key-fn
       :scroll-enabled                 @scroll-enabled?
       :on-scroll                      on-scroll
       :style                          {:height window-height}}]]))

(defn album-selector
  [sheet album? selected-album top]
  [:f> f-album-selector sheet album? selected-album top])
