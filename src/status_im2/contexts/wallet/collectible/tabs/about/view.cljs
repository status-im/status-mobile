(ns status-im2.contexts.wallet.collectible.tabs.about.view
  (:require [quo.components.markdown.text :as text]
            [quo.components.settings.section-label.view :as section-label]
            [quo.theme]
            [react-native.core :as rn] 
            [status-im2.contexts.wallet.collectible.tabs.about.style :as style]))

(defn- view-internal
  []
  [:<>
   [rn/view {:style style/title}
    [text/text
     {:size   :heading-2
      :weight :semi-bold}
     "Bored Ape Yacht Club"]]
   [rn/view {:style style/description}
    [text/text
     {:size :paragraph-2}
     "The Bored Ape Yacht Club is a collection of 10,000 unique Bored Ape NFTs— unique digital collectibles living on the Ethereum blockchain. Your Bored Ape doubles as your Yacht Club membership card, and grants access to members-only benefits, the first of which is access to THE BATHROOM, a collaborative graffiti board. Future areas and perks can be unlocked by the community through roadmap activation. Visit www.BoredApeYachtClub.com for more details."]]
   [section-label/view {:container-style style/section-label
                        :section         "On the web"}]])

(def view (quo.theme/with-theme view-internal))
