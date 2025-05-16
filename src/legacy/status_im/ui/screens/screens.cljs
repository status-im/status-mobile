(ns legacy.status-im.ui.screens.screens
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.screens.appearance.views :as appearance]
    [legacy.status-im.ui.screens.bug-report :as bug-report]
    [legacy.status-im.ui.screens.communities.members :as members]
    [legacy.status-im.ui.screens.help-center.views :as help-center]
    [legacy.status-im.ui.screens.pairing.views :as pairing]
    [legacy.status-im.ui.screens.profile.user.views :as profile.user]
    [legacy.status-im.ui.screens.progress.views :as progress]
    [legacy.status-im.ui.screens.rpc-usage-info :as rpc-usage-info]
    [react-native.platform :as platform]
    [utils.i18n :as i18n]))

(defn topbar-options
  [title]
  {:elevation        0
   :title            {:color (if (colors/dark?) colors/white colors/black)
                      :text  (i18n/label title)}
   :rightButtonColor (if (colors/dark?) colors/white colors/black)
   :background       {:color (if (colors/dark?) colors/black colors/white)}
   :backButton       {:color           (if (colors/dark?) colors/white colors/black)
                      :id              :legacy-back-button
                      :testID          :back-button
                      :visible         true
                      :popStackOnPress false}})

(defn screens
  []
  [;;PROGRESS
   {:name      :screen/progress
    :options   {:insets {:top? true}
                :theme  :dark}
    :component progress/progress}

   ;;COMMUNITY
   {:name      :screen/legacy-community-members
    :options   {:insets {:top? true}}
    :component members/view}


   ;;SETTINGS
   {:name      :screen/legacy-appearance
    :options   {:topBar {:visible false}
                :insets {:top? platform/android?}}
    :component appearance/appearance-view}

   ;; LEGACY SETTINGS
   {:name      :screen/legacy-settings
    :options   {:topBar {:visible false}
                :insets {:top? platform/android?}}
    :component profile.user/legacy-settings}

   ;; STATUS HELP
   {:name      :screen/help-center
    :options   {:topBar {:visible false}
                :insets {:top? platform/android?}}
    :component help-center/help-center}

   {:name      :screen/bug-report
    :options   {:topBar {:visible false}
                :insets {:top? platform/android?}}
    :component bug-report/bug-report}

   ;; OTHER
   {:name      :screen/installations
    :options   {:topBar (topbar-options (i18n/label :t/devices))
                :insets {:top? true}}
    :component pairing/installations}
   {:name      :screen/rpc-usage-info
    :options   {:topBar (topbar-options :t/rpc-usage-info)
                :insets {:top? true}}
    :component rpc-usage-info/usage-info}])
