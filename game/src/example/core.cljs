(ns example.core
  (:require [reagent.core :as r]))

(defonce !app-state (r/atom nil))

(defn app []
  (let [{:keys [message]} @!app-state]
    [:div [:p (or message "hello")]
     [:button {:on-click #(-> (js/fetch "/api/hello")
                              (.then (fn [r]
                                       (.text r)))
                              (.then js/alert))}
      "Talk to server"]]))

(defn koedbolle
  [{:keys [move?]}]
  (r/with-let [!state (r/atom 0)
               files ["images/koedbolle2.png"
                      "images/koedbolle3.png"]
               x (js/setInterval #(swap! !state inc) 200)]
    (let [image (if move?
                  (get files (mod (or @!state 0) (count files)))
                  "images/koedbolle1.png")]

      [:img {:src image}])
    (finally
      (js/clearInterval x)
      (println 'hello))))

(def key-codes
  {:left-arrow 37
   :right-arrow 39})

(defn gaffel [])

(defn game []
  (r/with-let [!state (r/atom {:pos [20 0]
                               :keys #{}})
               up-handler (fn [e] (swap! !state update :keys disj e.keyCode))
               down-handler (fn [e] (swap! !state update :keys conj e.keyCode))
               _ (js/window.addEventListener "keydown" down-handler)
               _ (js/window.addEventListener "keyup" up-handler)
               x (js/setInterval (fn []
                                   (when (contains? (:keys @!state) (key-codes :left-arrow))
                                     (swap! !state update :pos (fn [[x y]]
                                                                 [(if (> x 0)
                                                                    (dec x)
                                                                    x)
                                                                  y])))
                                   (when (contains? (:keys @!state) (key-codes :right-arrow))
                                     (swap! !state update :pos (fn [[x y]]
                                                                 [(if (< x 100)
                                                                    (inc x)
                                                                    x)
                                                                  y]))))

                                 100)]
    (let [{[x y] :pos} @!state]

      [:div
       [:div
        [gaffel]]
       [:div
        {:style {:position :absolute
                 :left     (str x "vw")}}
        [koedbolle {:move? (or (contains? (:keys @!state) (key-codes :left-arrow))
                               (contains? (:keys @!state) (key-codes :right-arrow)))}]]]
      #_[:div [:pre (pr-str @!state)]])
    (finally
      (js/clearInterval x)
      (js/window.removeEventListener "keydown" down-handler)
      (js/window.removeEventListener "keyup" up-handler))))

(defn ^:dev/after-load main []
  (r/render [game] (js/document.getElementById "app")))

