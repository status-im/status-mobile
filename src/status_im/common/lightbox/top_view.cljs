(ns status-im.common.lightbox.top-view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.orientation :as orientation]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [status-im.common.lightbox.animations :as anim]
    [status-im.common.lightbox.constants :as constants]
    [status-im.common.lightbox.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.url :as url]))

(defn animate-rotation
  [result screen-width screen-height insets
   {:keys [rotate top-view-y top-view-x top-view-width top-view-bg]}]
  (let [top-x (+ (/ constants/top-view-height 2) (:top insets))]
    (cond
      (= result orientation/landscape-left)
      (do
        (anim/animate rotate "90deg")
        (anim/animate top-view-y 60)
        (anim/animate top-view-x (- (/ screen-height 2) top-x))
        (anim/animate top-view-width screen-height)
        (anim/animate top-view-bg colors/neutral-100-opa-70))
      (= result orientation/landscape-right)
      (do
        (anim/animate rotate "-90deg")
        (anim/animate top-view-y (- (- screen-width) 4))
        (anim/animate top-view-x (+ (/ screen-height -2) top-x))
        (anim/animate top-view-width screen-height)
        (anim/animate top-view-bg colors/neutral-100-opa-70))
      (= result orientation/portrait)
      (do
        (anim/animate rotate "0deg")
        (anim/animate top-view-y 0)
        (anim/animate top-view-x 0)
        (anim/animate top-view-width screen-width)
        (anim/animate top-view-bg colors/neutral-100-opa-0)))))

(defn drawer
  [images index]
  (let [{:keys [image]} (nth images index)
        uri             (url/replace-port image (rf/sub [:mediaserver/port]))]
    [quo/action-drawer
     [[{:icon                :i/save
        :accessibility-label :save-image
        :label               (i18n/label :t/save-image-library)
        :on-press            (fn []
                               (rf/dispatch [:hide-bottom-sheet])
                               (rf/dispatch
                                [:lightbox/save-image-to-gallery
                                 uri
                                 #(rf/dispatch [:toasts/upsert
                                                {:id              :random-id
                                                 :type            :positive
                                                 :container-style {:bottom (when platform/android? 20)}
                                                 :text            (i18n/label :t/photo-saved)}])]))}]]]))

(defn share-image
  [images index]
  (let [{:keys [image]} (nth images index)
        uri             (url/replace-port image (rf/sub [:mediaserver/port]))]
    (rf/dispatch [:lightbox/share-image uri])))

(defn top-view
  [images insets index animations derived landscape? screen-width]
  (let [{:keys [description header]} (nth images @index)
        bg-color                     (if landscape?
                                       colors/neutral-100-opa-70
                                       colors/neutral-100-opa-0)
        {:keys [background-color opacity
                overlay-opacity]}    animations]
    [reanimated/view
     {:style
      (style/top-view-container (:top insets) screen-width bg-color landscape? animations derived)}
     [reanimated/linear-gradient
      {:colors [(colors/alpha "#000000" 0.8) :transparent]
       :start  {:x 0 :y 0}
       :end    {:x 0 :y 1}
       :style  (style/top-gradient insets)}]
     [rn/view
      {:style {:flex-direction :row
               :align-items    :center}}
      [rn/touchable-opacity
       {:on-press (fn []
                    (anim/animate background-color :transparent)
                    (anim/animate opacity 0)
                    (anim/animate overlay-opacity 0)
                    (rf/dispatch (if platform/ios?
                                   [:lightbox/exit-lightbox-signal @index]
                                   [:navigate-back])))
        :style    style/close-container}
       [quo/icon :close {:size 20 :color colors/white}]]
      [rn/view {:style {:margin-left 12}}
       [quo/text
        {:weight :semi-bold
         :size   :paragraph-1
         :style  {:color colors/white}} header]
       [quo/text
        {:weight :medium
         :size   :paragraph-2
         :style  {:color colors/neutral-40}} description]]]
     [rn/view {:style style/top-right-buttons}
      [rn/touchable-opacity
       {:active-opacity      1
        :accessibility-label :share-image
        :on-press            #(share-image images @index)
        :style               (merge style/close-container {:margin-right 12})}
       [quo/icon :share {:size 20 :color colors/white}]]
      [rn/touchable-opacity
       {:active-opacity      1
        :accessibility-label :image-options
        :on-press            #(rf/dispatch [:show-bottom-sheet
                                            {:content (fn [] [drawer images @index])}])
        :style               style/close-container}
       [quo/icon :options {:size 20 :color colors/white}]]]]))
