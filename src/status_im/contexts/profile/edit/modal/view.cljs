(ns status-im.contexts.profile.edit.modal.view
  (:require
    [clojure.string :as string]
    [oops.core :as oops]
    [quo.context]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.hooks :as hooks]
    [react-native.platform :as platform]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im.common.avatar-picture-picker.view :as profile-picture-picker]
    [status-im.common.events-helper :as events-helper]
    [status-im.common.validation.profile :as profile-validator]
    [status-im.constants :as c]
    [status-im.contexts.profile.edit.modal.events]
    [status-im.contexts.profile.edit.modal.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.responsiveness :as responsiveness]))

(def scroll-view-height (reagent/atom 0))
(def content-container-height (reagent/atom 0))
(def set-scroll-view-height #(reset! scroll-view-height %))

(defn set-content-container-height
  [e]
  (reset! content-container-height (oops/oget e "nativeEvent.layout.height")))

(defn show-button-background?
  [keyboard-height keyboard-shown content-scroll-y]
  (let [button-container-height 64
        keyboard-view-height    (+ keyboard-height button-container-height)]
    (when keyboard-shown
      (cond
        platform/android?
        (< (- @scroll-view-height button-container-height) @content-container-height)

        platform/ios?
        (< (- @scroll-view-height keyboard-view-height) (- @content-container-height content-scroll-y))

        :else
        false))))

(defn button-container
  [show-keyboard? keyboard-shown show-background? keyboard-height children]
  (let [height (reagent/atom 0)]
    (reset! height (if show-keyboard? (if keyboard-shown keyboard-height 0) 0))
    [rn/view {:style {:margin-top :auto}}
     (cond
       (and (> @height 0) show-background?)
       [quo/blur
        (when keyboard-shown
          {:blur-amount      34
           :blur-type        :transparent
           :overlay-color    :transparent
           :background-color (if platform/android?
                               colors/neutral-100
                               colors/neutral-80-opa-1-blur)
           :style            style/blur-button-container})
        children]

       (and (> @height 0) (not show-background?))
       [rn/view {:style (style/view-button-container true)}
        children]

       (not show-keyboard?)
       [rn/view {:style (style/view-button-container false)}
        children])]))

(defn- add-keyboard-listener
  [e callback]
  (oops/ocall rn/keyboard "addListener" e callback))

(defn- on-keyboard-show
  [f]
  (add-keyboard-listener (if platform/android? "keyboardDidShow" "keyboardWillShow") f))

(defn- on-keyboard-hide
  [f]
  (add-keyboard-listener (if platform/android? "keyboardDidHide" "keyboardWillHide") f))

(defn floating-button
  [{:keys [custom-color scroll-y on-submit disabled?]}]
  (reagent/with-let [show-keyboard? (reagent/atom false)
                     show-listener  (on-keyboard-show #(reset! show-keyboard? true))
                     hide-listener  (on-keyboard-hide #(reset! show-keyboard? false))]
    (let [{:keys [keyboard-shown keyboard-height]} (hooks/use-keyboard)
          show-background?                         (show-button-background? keyboard-height
                                                                            keyboard-shown
                                                                            scroll-y)]
      [rn/keyboard-avoiding-view
       {:style          {:position :absolute
                         :top      0
                         :bottom   0
                         :left     0
                         :right    0}
        :pointer-events :box-none}
       [button-container @show-keyboard? keyboard-shown show-background? keyboard-height
        [quo/button
         {:accessibility-label :submit-create-profile-button
          :type                :primary
          :customization-color custom-color
          :on-press            on-submit
          :container-style     style/continue-button
          :disabled?           disabled?}
         (i18n/label :t/continue)]]])
    (finally
     (oops/ocall show-listener "remove")
     (oops/ocall hide-listener "remove"))))

(defn- content
  [{:keys [set-display-name set-validation-msg picture-picker]}]
  (let [open-picture-picker (fn []
                              (rf/dispatch [:dismiss-keyboard])
                              (rf/dispatch [:show-bottom-sheet
                                            {:content picture-picker
                                             :shell?  true
                                             :theme   :dark}]))
        on-change-text      (fn [s]
                              (set-validation-msg s)
                              (set-display-name s))]
    (fn [{:keys [profile-image custom-color display-name validation-msg valid-name?
                 name-too-short? initial-display-name set-scroll set-scroll-height
                 set-custom-color]}]
      (let [{window-width :width} (rn/get-window)
            info-type             (cond
                                    validation-msg  :error
                                    name-too-short? :default
                                    :else           :success)
            info-message          (or validation-msg
                                      (i18n/label :t/minimum-characters
                                                  {:min-chars profile-validator/min-length}))
            full-name             (if (seq display-name)
                                    display-name
                                    initial-display-name)]
        [rn/scroll-view
         {:on-layout               set-scroll-height
          :on-scroll               set-scroll
          :scroll-event-throttle   64
          :content-container-style {:flex-grow 1}}
         [rn/view {:on-layout set-content-container-height}
          [rn/view {:style style/content-container}
           [quo/text
            {:style  style/title
             :size   :heading-1
             :weight :semi-bold}
            (i18n/label :t/edit-profile)]
           [rn/view {:style style/input-container}
            [rn/view {:style style/profile-input-container}
             [quo/profile-input
              {:customization-color custom-color
               :placeholder         initial-display-name
               :on-press            open-picture-picker
               :image-picker-props  {:profile-picture     profile-image
                                     :full-name           full-name
                                     :customization-color custom-color}
               :title-input-props   {:default-value  ""
                                     :auto-focus     true
                                     :max-length     c/profile-name-max-length
                                     :on-change-text on-change-text}}]]
            [quo/info-message
             {:status          info-type
              :size            :default
              :icon            (if valid-name? :i/positive-state :i/info)
              :color           (when (= :default info-type) colors/white-70-blur)
              :container-style style/info-message}
             info-message]
            [quo/text
             {:size   :paragraph-2
              :weight :medium
              :style  style/color-title}
             (i18n/label :t/colour)]]]
          [quo/color-picker
           {:blur?            true
            :default-selected custom-color
            :on-change        set-custom-color
            :window-width     window-width
            :container-style  {:padding-left (responsiveness/iphone-11-Pro-20-pixel-from-width
                                              window-width)}}]]]))))

(defn- submittable-state?
  [{:keys [new-color? new-picture? new-name? valid-name?]}]
  (let [acceptable-name? (or (not new-name?) valid-name?)]
    (or (and new-color? acceptable-name?)
        (and new-picture? acceptable-name?)
        (and new-name? valid-name?))))

(defn view
  []
  (let [{:keys [pending-event]}        (quo.context/use-screen-params)
        {initial-display-name :display-name
         initial-color        :customization-color
         [initial-image]      :images} (rf/sub [:profile/profile])
        top                            (safe-area/get-top)
        scroll-y                       (reagent/atom 0)
        display-name                   (reagent/atom "")
        validation-msg                 (reagent/atom (profile-validator/validation-name @display-name))
        custom-color                   (reagent/atom (or initial-color c/profile-default-color))
        profile-image                  (reagent/atom initial-image)
        set-display-name               #(reset! validation-msg (profile-validator/validation-name %))
        set-validation-msg             #(reset! display-name (string/trim %))
        set-custom-color               #(reset! custom-color %)
        set-scroll                     (fn [e]
                                         (reset! scroll-y (oops/oget e "nativeEvent.contentOffset.y")))
        set-scroll-height              (fn [e]
                                         (reset! scroll-y 0)
                                         (-> e
                                             (oops/oget "nativeEvent.layout.height")
                                             set-scroll-view-height))
        picture-picker                 (fn []
                                         [profile-picture-picker/view
                                          {:on-result    #(reset! profile-image %)
                                           :has-picture? (some? @profile-image)}])
        update-profile                 (fn []
                                         (rf/dispatch
                                          [:profile/edit-profile
                                           (cond-> {:on-success pending-event}
                                             (and (seq @display-name)
                                                  (not= initial-display-name @display-name))
                                             (assoc :display-name @display-name)

                                             (not= initial-image @profile-image)
                                             (assoc :picture @profile-image)

                                             (not= initial-color @custom-color)
                                             (assoc :color @custom-color))]))
        on-close                       (fn []
                                         (events-helper/navigate-back)
                                         (rf/dispatch pending-event))]
    (fn []
      (let [name-too-short? (profile-validator/name-too-short? @display-name)
            valid-name?     (and (not @validation-msg) (not name-too-short?))
            disabled?       (not
                             (submittable-state?
                              {:new-color?   (not= initial-color @custom-color)
                               :new-picture? (not= @profile-image initial-image)
                               :new-name?    (and (not= initial-display-name @display-name)
                                                  (seq @display-name))
                               :valid-name?  valid-name?}))]
        [quo/overlay {:type :shell}
         [rn/view {:style style/page-container}
          [quo/page-nav
           {:margin-top top
            :background :blur
            :icon-name  :i/close
            :on-press   on-close}]
          [content
           {:profile-image        @profile-image
            :custom-color         @custom-color
            :display-name         @display-name
            :validation-msg       @validation-msg
            :valid-name?          valid-name?
            :name-too-short?      name-too-short?
            :picture-picker       picture-picker
            :initial-display-name initial-display-name
            :set-scroll           set-scroll
            :set-scroll-height    set-scroll-height
            :set-custom-color     set-custom-color
            :set-display-name     set-display-name
            :set-validation-msg   set-validation-msg}]
          [floating-button
           {:custom-color @custom-color
            :scroll-y     @scroll-y
            :on-submit    update-profile
            :disabled?    disabled?}]]]))))
