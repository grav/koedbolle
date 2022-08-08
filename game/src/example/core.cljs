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

(defn gaffel-view [{:keys [x]}]
  (r/with-let [!state (r/atom 0)
               height 484
               id (js/setInterval (fn []
                                    (let [p @!state]
                                      (when (< p 100)
                                        (swap! !state inc)))))]

    (let [y (- 0 (* (- 1 (/ @!state 100)) height))]
      [:div {:style {:position :relative
                     :top (str y "px")
                     :left x}}
       [:img {:src "images/gaffel.png"}]])
    (finally
      (js/clearInterval id))))

(defn game []
  (r/with-let [!state (r/atom {:pos [20 0]
                               :tick 0
                               :keys #{}})
               up-handler (fn [e] (swap! !state update :keys disj e.keyCode))
               down-handler (fn [e] (swap! !state update :keys conj e.keyCode))
               _ (js/window.addEventListener "keydown" down-handler)
               _ (js/window.addEventListener "keyup" up-handler)
               id (js/setInterval (fn []
                                    (let [{:keys [tick] :as state} @!state
                                          new-state (cond-> (update state :tick inc)

                                                            (contains? (:keys state) (key-codes :left-arrow))
                                                            (update :pos (fn [[x y]]
                                                                           [(if (> x 0)
                                                                              (dec x)
                                                                              x)
                                                                            y]))

                                                            (contains? (:keys state) (key-codes :right-arrow))
                                                            (update :pos (fn [[x y]]
                                                                           [(if (< x 100)
                                                                              (inc x)
                                                                              x)
                                                                            y]))

                                                            (zero? (mod tick 50))
                                                            (update :gaffel #(rand-int 1000))

                                                            #_#_(not (zero? (mod tick 100)))
                                                            (dissoc :gaffel))]


                                      (reset! !state new-state)))

                                  50)]
    (let [{[x y] :pos
           :keys [gaffel tick]} @!state]

      [:div
       [:div
        [:h1 (str "Tid: " (js/Math.floor (/ tick 20)) " sekunder")]]
       [:div
        {:style {:position :absolute
                 :left (str 10 "vw")}}
        [gaffel-view {:x gaffel}]]
       [:div
        {:style {:position :absolute
                 :left     (str x "vw")
                 :top 250}}
        [koedbolle {:move? (or (contains? (:keys @!state) (key-codes :left-arrow))
                               (contains? (:keys @!state) (key-codes :right-arrow)))}]]]
      #_[:div [:pre (pr-str @!state)]])
    (finally
      (js/clearInterval id)
      (js/window.removeEventListener "keydown" down-handler)
      (js/window.removeEventListener "keyup" up-handler))))

(defn ^:dev/after-load main []
  (r/render [game] (js/document.getElementById "app")))

