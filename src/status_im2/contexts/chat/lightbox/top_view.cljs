(ns status-im2.contexts.chat.lightbox.top-view
  (:require
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.orientation :as orientation]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [status-im2.contexts.chat.lightbox.animations :as anim]
    [status-im2.contexts.chat.lightbox.style :as style]
    [taoensso.timbre :as log]
    [utils.datetime :as datetime]
    [utils.re-frame :as rf]
    [status-im2.contexts.chat.lightbox.constants :as c]
    [react-native.share :as share]
    ["react-native-blob-util" :default ReactNativeBlobUtil]
    [react-native.fs :as fs]
    [status-im.utils.http :as http]))

(defn animate-rotation
  [result screen-width screen-height insets
   {:keys [rotate top-view-y top-view-x top-view-width top-view-bg]}]
  (let [top-x (+ (/ c/top-view-height 2) (:top insets))]
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

(defn download-image-http
  [base64-uri on-success]
  (-> (.config ReactNativeBlobUtil (clj->js {:trusty platform/ios?}))
      (.fetch "GET" (str base64-uri))
      (.then #(on-success (.base64 ^js %)))
      (.catch #(log/error "could not fetch audio " base64-uri))))

(defn top-view
  [messages insets index animations derived landscape? screen-width]
  (let [{:keys [from timestamp content]} (nth @messages @index)
        display-name                       (first (rf/sub [:contacts/contact-two-names-by-identity
                                                           from]))
        bg-color                           (if landscape?
                                             colors/neutral-100-opa-70
                                             colors/neutral-100-opa-0)
        {:keys [background-color opacity]} animations]
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
                    (rf/dispatch (if platform/ios?
                                   [:chat.ui/exit-lightbox-signal @index]
                                   [:navigate-back])))
        :style    style/close-container}
       [quo/icon :close {:size 20 :color colors/white}]]
      [rn/view {:style {:margin-left 12}}
       [quo/text
        {:weight :semi-bold
         :size   :paragraph-1
         :style  {:color colors/white}} display-name]
       [quo/text
        {:weight :medium
         :size   :paragraph-2
         :style  {:color colors/neutral-40}} (datetime/to-short-str timestamp)]]]
     [rn/view {:style style/top-right-buttons}
      [rn/touchable-opacity
       {:active-opacity 1
        :on-press  (fn []
                     (download-image-http (:image content) (fn [param]
                                                             (share/open {:url (str "data:image/jpeg;base64," param)}
                                                                                   #(println "success")#(println"faill")))))
        :style          (merge style/close-container {:margin-right 12})}
       [quo/icon :share {:size 20 :color colors/white}]]
      [rn/touchable-opacity
       {:active-opacity 1
        :on-press (fn [] (rf/dispatch [:show-bottom-sheet {:content [rn/view [rn/text "yoo"]]}]))
        :style          style/close-container}
       [quo/icon :options {:size 20 :color colors/white}]]]]))
