(ns status-im2.contexts.onboarding.create-profile.view
  (:require [quo2.core :as quo]
            [clojure.string :as string]
            [quo2.foundations.colors :as colors]
            [status-im2.contexts.onboarding.create-profile.style :as style]
            [utils.i18n :as i18n]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [reagent.core :as reagent]
            [status-im2.contexts.onboarding.common.navigation-bar.view :as navigation-bar]
            [status-im2.contexts.onboarding.common.background.view :as background]
            [status-im2.contexts.onboarding.select-photo.method-menu.view :as method-menu]
            [utils.re-frame :as rf]
            [oops.core :as oops]
            [react-native.blur :as blur]
            [status-im2.constants :as c]))

(def emoji-regex
  (new
   js/RegExp
   #"(\u00a9|\u00ae|[\u2000-\u3300]|\ud83c[\ud000-\udfff]|\ud83d[\ud000-\udfff]|\ud83e[\ud000-\udfff])"
   "i"))
(defn has-emojis [s] (re-find emoji-regex s))
(def common-names ["Ethereum" "Bitcoin"])
(defn has-common-names [s] (pos? (count (filter #(string/includes? s %) common-names))))
(def special-characters-regex (new js/RegExp #"[^a-zA-Z\d\s-._]" "i"))
(defn has-special-characters [s] (re-find special-characters-regex s))
(def min-length 5)
(defn length-not-valid [s] (< (count (string/trim (str s))) min-length))


(defn validation-message
  [s]
  (cond
    (or (= s nil) (= s ""))      nil
    (has-special-characters s)   (i18n/label :t/are-not-allowed
                                             {:check (i18n/label :t/special-characters)})
    (string/ends-with? s "-eth") (i18n/label :t/ending-not-allowed {:ending "-eth"})
    (string/ends-with? s "_eth") (i18n/label :t/ending-not-allowed {:ending "_eth"})
    (string/ends-with? s ".eth") (i18n/label :t/ending-not-allowed {:ending ".eth"})
    (has-common-names s)         (i18n/label :t/are-not-allowed {:check (i18n/label :t/common-names)})
    (has-emojis s)               (i18n/label :t/are-not-allowed {:check (i18n/label :t/emojis)})
    :else                        nil))

(defn button-container
  [keyboard-shown? children]
  [rn/view {:style {:margin-top :auto}}
   (if keyboard-shown?
     [blur/ios-view
      {:blur-amount      34
       :blur-type        :transparent
       :overlay-color    :transparent
       :background-color colors/neutral-80-opa-1-blur
       :style            style/button-container}
      children]
     [rn/view {:style style/view-button-container}
      children])])

(defn- f-page
  [{:keys [onboarding-profile-data navigation-bar-top]}]
  (reagent/with-let [keyboard-shown?                         (reagent/atom false)
                     will-show-listener                      (oops/ocall rn/keyboard
                                                                         "addListener"
                                                                         "keyboardWillShow"
                                                                         #(reset! keyboard-shown? true))
                     will-hide-listener                      (oops/ocall rn/keyboard
                                                                         "addListener"
                                                                         "keyboardWillHide"
                                                                         #(reset! keyboard-shown? false))
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
                     on-change                               #(reset! custom-color %)
                     name-too-short?                         (length-not-valid @full-name)
                     valid-name?                             (not (or @validation-msg name-too-short?))]
    [rn/view {:style style/page-container}
     [navigation-bar/navigation-bar {:top navigation-bar-top}]
     [rn/scroll-view
      {:keyboard-should-persist-taps :always
       :content-container-style      {:flex-grow 1}}
      [rn/view {:style style/page-container}
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
                                     {:override-theme :dark
                                      :content
                                      (fn []
                                        [method-menu/view on-change-profile-pic])}]))
            :image-picker-props  {:profile-picture     (when @profile-pic {:uri @profile-pic})
                                  :full-name           (if (seq @full-name)
                                                         @full-name
                                                         (i18n/label :t/your-name))
                                  :customization-color @custom-color}
            :title-input-props   {:default-value  @full-name
                                  :auto-focus     true
                                  :max-length     c/profile-name-max-length
                                  :on-change-text on-change-text}}]]

         [quo/info-message
          {:type       (cond @validation-msg :error
                             name-too-short? :neutral
                             :else           :positive)
           :size       :default
           :icon       (if valid-name? :i/positive-state :i/info)
           :text-color (when name-too-short? colors/white-70-blur)
           :icon-color (when name-too-short? colors/white-70-blur)
           :style      style/info-message}
          (if @validation-msg
            @validation-msg
            (i18n/label :t/minimum-characters
                        {:min-chars min-length}))]
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
      [button-container @keyboard-shown?
       [quo/button
        {:accessibility-label       :submit-create-profile-button
         :type                      :primary
         :override-background-color (colors/custom-color @custom-color 60)
         :on-press                  (fn []
                                      (rf/dispatch [:onboarding-2/profile-data-set
                                                    {:image-path   @profile-pic
                                                     :display-name @full-name
                                                     :color        @custom-color}]))
         :style                     style/continue-button
         :disabled                  (or (not (seq @full-name)) @validation-msg)}
        (i18n/label :t/continue)]]]]
    (finally
     (oops/ocall will-show-listener "remove")
     (oops/ocall will-hide-listener "remove"))))

(defn create-profile
  []
  (let [{:keys [top]}           (safe-area/get-insets)
        onboarding-profile-data (rf/sub [:onboarding-2/profile])]
    [:<>
     [background/view true]
     [:f> f-page
      {:navigation-bar-top      top
       :onboarding-profile-data onboarding-profile-data}]]))
