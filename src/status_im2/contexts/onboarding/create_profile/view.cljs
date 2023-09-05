(ns status-im2.contexts.onboarding.create-profile.view
  (:require [clojure.string :as string]
            [oops.core :as oops]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.blur :as blur]
            [react-native.core :as rn]
            [react-native.hooks :as hooks]
            [react-native.platform :as platform]
            [react-native.safe-area :as safe-area]
            [reagent.core :as reagent]
            [status-im2.constants :as c]
            [status-im2.contexts.onboarding.create-profile.style :as style]
            [status-im2.contexts.onboarding.select-photo.method-menu.view :as method-menu]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

;; NOTE - validation should match with Desktop
;; https://github.com/status-im/status-desktop/blob/2ba96803168461088346bf5030df750cb226df4c/ui/imports/utils/Constants.qml#L468
;; 
(def emoji-regex
  (new
   js/RegExp
   #"(\u00a9|\u00ae|[\u2000-\u3300]|\ud83c[\ud000-\udfff]|\ud83d[\ud000-\udfff]|\ud83e[\ud000-\udfff])"
   "i"))
(defn has-emojis [s] (re-find emoji-regex s))
(def common-names ["Ethereum" "Bitcoin"])
(defn has-common-names [s] (pos? (count (filter #(string/includes? s %) common-names))))
(def status-regex (new js/RegExp #"^[a-zA-Z0-9\-_ ]+$"))
(defn has-special-characters [s] (not (re-find status-regex s)))
(def min-length 5)
(defn length-not-valid [s] (< (count (string/trim (str s))) min-length))
(def scroll-view-height (reagent/atom 0))
(def content-container-height (reagent/atom 0))

(defn validation-message
  [s]
  (cond
    (or (= s nil) (= s ""))      nil
    (has-special-characters s)   (i18n/label :t/are-not-allowed
                                             {:check (i18n/label :t/special-characters)})
    (string/ends-with? s "-eth") (i18n/label :t/ending-not-allowed {:ending "-eth"})
    (string/ends-with? s "_eth") (i18n/label :t/ending-not-allowed {:ending "_eth"})
    (string/ends-with? s ".eth") (i18n/label :t/ending-not-allowed {:ending ".eth"})
    (string/starts-with? s " ")  (i18n/label :t/start-with-space)
    (string/ends-with? s " ")    (i18n/label :t/ends-with-space)
    (has-common-names s)         (i18n/label :t/are-not-allowed {:check (i18n/label :t/common-names)})
    (has-emojis s)               (i18n/label :t/are-not-allowed {:check (i18n/label :t/emojis)})
    :else                        nil))


(defn show-button-background
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
       [blur/ios-view
        (when keyboard-shown
          {:blur-amount      34
           :blur-type        :transparent
           :overlay-color    :transparent
           :background-color (if platform/android? colors/neutral-100 colors/neutral-80-opa-1-blur)
           :style            style/blur-button-container})
        children]

       (and (> @height 0) (not show-background?))
       [rn/view {:style (style/view-button-container true)}
        children]

       (not show-keyboard?)
       [rn/view {:style (style/view-button-container false)}
        children])]))

(defn- f-page
  [{:keys [onboarding-profile-data navigation-bar-top]}]
  (reagent/with-let [show-keyboard?                          (reagent/atom false)
                     content-scroll-y                        (reagent/atom 0)
                     show-listener                           (oops/ocall rn/keyboard
                                                                         "addListener"
                                                                         (if platform/android?
                                                                           "keyboardDidShow"
                                                                           "keyboardWillShow")
                                                                         #(reset! show-keyboard? true))
                     hide-listener                           (oops/ocall rn/keyboard
                                                                         "addListener"
                                                                         (if platform/android?
                                                                           "keyboardDidHide"
                                                                           "keyboardWillHide")
                                                                         #(reset! show-keyboard? false))
                     {:keys [image-path display-name color]} onboarding-profile-data
                     full-name                               (reagent/atom display-name)
                     validation-msg                          (reagent/atom (validation-message
                                                                            @full-name))
                     on-change-text                          (fn [s]
                                                               (reset! validation-msg (validation-message
                                                                                       s))
                                                               (reset! full-name (string/trim s)))
                     custom-color                            (reagent/atom (or color
                                                                               c/profile-default-color))
                     profile-pic                             (reagent/atom image-path)
                     on-change-profile-pic                   #(reset! profile-pic %)
                     on-change                               #(reset! custom-color %)]
    (let [name-too-short?                          (length-not-valid @full-name)
          valid-name?                              (and (not @validation-msg) (not name-too-short?))
          info-message                             (if @validation-msg
                                                     @validation-msg
                                                     (i18n/label :t/minimum-characters
                                                                 {:min-chars min-length}))
          info-type                                (cond @validation-msg :error
                                                         name-too-short? :default
                                                         :else           :success)
          {:keys [keyboard-shown keyboard-height]} (hooks/use-keyboard)
          show-background?                         (show-button-background keyboard-height
                                                                           keyboard-shown
                                                                           @content-scroll-y)]
      [rn/view {:style style/page-container}
       [quo/page-nav
        {:margin-top navigation-bar-top
         :background :blur
         :icon-name  :i/arrow-left
         :on-press   #(rf/dispatch [:navigate-back-within-stack :new-to-status])}]
       [rn/scroll-view
        {:on-layout               (fn [event]
                                    (let [height (oops/oget event "nativeEvent.layout.height")]
                                      (reset! scroll-view-height height)
                                      (reset! content-scroll-y 0)))
         :on-scroll               (fn [event]
                                    (let [y (oops/oget event "nativeEvent.contentOffset.y")]
                                      (reset! content-scroll-y y)))
         :scroll-event-throttle   64
         :content-container-style {:flexGrow 1}}
        [rn/view
         {:on-layout (fn [event]
                       (let [height (oops/oget event "nativeEvent.layout.height")]
                         (reset! content-container-height height)))}
         [rn/view
          {:style style/content-container}
          [quo/text
           {:size   :heading-1
            :weight :semi-bold
            :style  style/title} (i18n/label :t/create-profile)]
          [rn/view
           {:style style/input-container}
           [rn/view
            {:style style/profile-input-container}
            [quo/profile-input
             {:customization-color @custom-color
              :placeholder         (i18n/label :t/your-name)
              :on-press            (fn []
                                     (rf/dispatch [:dismiss-keyboard])
                                     (rf/dispatch
                                      [:show-bottom-sheet
                                       {:content
                                        (fn []
                                          [method-menu/view on-change-profile-pic])}]))
              :image-picker-props  {:profile-picture     (or
                                                          @profile-pic
                                                          (rf/sub
                                                           [:profile/onboarding-placeholder-avatar
                                                            @profile-pic]))
                                    :full-name           (if (seq @full-name)
                                                           @full-name
                                                           (i18n/label :t/your-name))
                                    :customization-color @custom-color}
              :title-input-props   {:default-value  @full-name
                                    :auto-focus     true
                                    :max-length     c/profile-name-max-length
                                    :on-change-text on-change-text}}]]

           [quo/info-message
            {:type       info-type
             :size       :default
             :icon       (if valid-name? :i/positive-state :i/info)
             :text-color (when (= :default info-type) colors/white-70-blur)
             :icon-color (when (= :default info-type) colors/white-70-blur)
             :style      style/info-message}
            info-message]
           [quo/text
            {:size   :paragraph-2
             :weight :medium
             :style  style/color-title}
            (i18n/label :t/accent-colour)]
           [quo/color-picker
            {:blur?             true
             :default-selected? :blue
             :selected          @custom-color
             :on-change         on-change}]]]]]

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
           :customization-color @custom-color
           :on-press            (fn []
                                  (rf/dispatch [:onboarding-2/profile-data-set
                                                {:image-path   @profile-pic
                                                 :display-name @full-name
                                                 :color        @custom-color}]))
           :container-style     style/continue-button
           :disabled?           (or (not valid-name?) (not (seq @full-name)))}
          (i18n/label :t/continue)]]]])
    (finally
     (oops/ocall show-listener "remove")
     (oops/ocall hide-listener "remove"))))

(defn create-profile
  []
  (let [{:keys [top]}           (safe-area/get-insets)
        onboarding-profile-data (rf/sub [:onboarding-2/profile])]
    [:<>
     [:f> f-page
      {:navigation-bar-top      top
       :onboarding-profile-data onboarding-profile-data}]]))
