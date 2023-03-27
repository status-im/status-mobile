(ns status-im2.contexts.onboarding.create-profile.view
  (:require [quo2.core :as quo]
            [clojure.string :as string]
            [quo2.foundations.colors :as colors]
            [status-im2.contexts.onboarding.create-profile.style :as style]
            [utils.i18n :as i18n]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.onboarding.common.background.view :as background]
            [status-im2.contexts.onboarding.select-photo.method-menu.view :as method-menu]
            [utils.re-frame :as rf]
            [status-im2.constants :as c]))

(defn navigation-bar
  []
  [rn/view {:style style/navigation-bar}
   [quo/page-nav
    {:align-mid?   true
     :mid-section  {:type :text-only :main-text ""}
     :left-section {:type                :blur-bg
                    :icon                :i/arrow-left
                    :icon-override-theme :dark
                    :on-press            #(rf/dispatch [:navigate-back])}}]])

(def emoji-regex
  (new
   js/RegExp
   #"(\u00a9|\u00ae|[\u2000-\u3300]|\ud83c[\ud000-\udfff]|\ud83d[\ud000-\udfff]|\ud83e[\ud000-\udfff])"
   "i"))
(defn has-emojis [s] (re-find emoji-regex s))
(def common-names ["Ethereum" "Bitcoin"])
(defn has-common-names [s] (pos? (count (filter #(string/includes? s %) common-names))))
(def special-characters-regex (new js/RegExp #"[^a-zA-Z\d\s-]" "i"))
(defn has-special-characters [s] (re-find special-characters-regex s))

(defn validation-message
  [s]
  (cond
    (or (= s nil) (= s ""))      nil
    (has-emojis s)               (i18n/label :t/are-not-allowed {:check (i18n/label :t/emojis)})
    (has-special-characters s)   (i18n/label :t/are-not-allowed
                                             {:check (i18n/label :t/special-characters)})
    (string/ends-with? s "-eth") (i18n/label :t/ending-not-allowed {:ending "-eth"})
    (has-common-names s)         (i18n/label :t/are-not-allowed {:check (i18n/label :t/common-names)})
    :else                        nil))

(defn page
  [{:keys [image-path display-name color]}]
  (let [full-name             (reagent/atom display-name)
        validation-msg        (reagent/atom (validation-message @full-name))
        on-change-text        (fn [s]
                                (reset! validation-msg (validation-message s))

                                (reset! full-name s))
        custom-color          (reagent/atom (or color c/profile-default-color))
        profile-pic           (reagent/atom image-path)
        on-change-profile-pic #(reset! profile-pic %)
        on-change             #(reset! custom-color %)]
    (fn []
      [rn/view {:style style/page-container}
       [navigation-bar]
       [rn/view
        {:style {:flex               1
                 :padding-horizontal 20}}
        [quo/text
         {:size   :heading-1
          :weight :semi-bold
          :style  {:color         colors/white
                   :margin-top    12
                   :margin-bottom 20}} (i18n/label :t/create-profile)]
        [rn/view
         {:flex        1
          :align-items :flex-start}

         [rn/view
          {:flex-direction  :row
           :justify-content :center}
          [quo/profile-input
           {:customization-color @custom-color
            :placeholder         (i18n/label :t/your-name)
            :on-press            #(rf/dispatch
                                   [:bottom-sheet/show-sheet
                                    {:override-theme :dark
                                     :content
                                     (fn []
                                       [method-menu/view on-change-profile-pic])}])
            :image-picker-props  {:profile-picture @profile-pic
                                  :full-name       @full-name}
            :title-input-props   {:max-length     c/profile-name-max-length
                                  :on-change-text on-change-text}}]]
         (when @validation-msg
           [quo/info-message
            {:type  :error
             :size  :default
             :icon  :i/info
             :style style/info-message}
            @validation-msg])
         [quo/text
          {:size  :paragraph-2
           :style {:color         colors/white-70-blur
                   :margin-top    20
                   :margin-bottom 16}} (i18n/label :t/accent-colour)]
         [quo/color-picker
          {:blur?             true
           :default-selected? :blue
           :selected          @custom-color
           :on-change         on-change}]]
        [quo/button
         {:accessibility-label       :submit-create-profile-button
          :type                      :primary
          :override-background-color (colors/custom-color @custom-color 60)
          :on-press                  #(rf/dispatch [:onboarding-2/profile-data-set
                                                    {:image-path   @profile-pic
                                                     :display-name @full-name
                                                     :color        @custom-color}])
          :style                     style/continue-button
          :disabled                  (and (not (seq @full-name)) (not @validation-msg))}
         (i18n/label :t/continue)]]])))

(defn create-profile
  []
  [rn/view {:style {:flex 1}}
   (let [onboarding-profile-data (rf/sub [:onboarding-2/profile])]
     [background/view true]
     [page onboarding-profile-data])])
