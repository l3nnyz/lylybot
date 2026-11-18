(() => {
    const $ = (id) => document.getElementById(id);
    const app = $("app");
    const saisie = $("saisie");
    const envoyer = $("btn-envoyer");
    const fil = $("fil");
    const historique = $("historique");
    const reduit = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    const stocke = {
        lire: (k) => { try { return localStorage.getItem(k); } catch { return null; } },
        ecrire: (k, v) => { try { localStorage.setItem(k, v); } catch {} },
    };

    const btnMenu = $("btn-menu");
    function regleMenu(ferme) {
        app.classList.toggle("menu-ferme", ferme);
        btnMenu.setAttribute("aria-expanded", String(!ferme));
        stocke.ecrire("lylybot.menu", ferme ? "ferme" : "ouvert");
    }
    regleMenu(stocke.lire("lylybot.menu") === "ferme");
    btnMenu.addEventListener("click", () => regleMenu(!app.classList.contains("menu-ferme")));

    historique.addEventListener("click", (e) => {
        const g = e.target.closest(".app-groupe");
        if (!g) return;
        g.classList.toggle("replie");
        let n = g.nextElementSibling;
        while (n && n.classList.contains("app-conv")) { n.classList.toggle("cache", g.classList.contains("replie")); n = n.nextElementSibling; }
    });

    function ajuste() {
        saisie.style.height = "auto";
        saisie.style.height = Math.min(saisie.scrollHeight, 200) + "px";
        envoyer.disabled = saisie.value.trim() === "";
    }
    saisie.addEventListener("input", ajuste);
    saisie.addEventListener("keydown", (e) => {
        if (e.key === "Enter" && !e.shiftKey) { e.preventDefault(); $("formulaire").requestSubmit(); }
    });
    saisie.focus();

    $("suggestions").addEventListener("click", (e) => {
        const b = e.target.closest("[data-texte]");
        if (!b) return;
        saisie.value = b.dataset.texte;
        ajuste();
        saisie.focus();
        saisie.setSelectionRange(saisie.value.length, saisie.value.length);
    });

    const exemples = ["Demandez n'importe quoi", "Expliquez-moi la photosynthèse simplement", "Aidez-moi à écrire un e-mail", "Donnez-moi des idées de recettes"];
    let ex = 0;
    if (!reduit) {
        setInterval(() => {
            if (saisie.value || document.activeElement === saisie || app.classList.contains("en-cours")) return;
            ex = (ex + 1) % exemples.length;
            saisie.placeholder = exemples[ex];
        }, 3500);
    }

    let titreAjoute = Boolean(app.dataset.conv);
    const iconeCopier = '<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><rect x="9" y="9" width="11" height="11" rx="2"/><path d="M5 15V6a2 2 0 0 1 2-2h9"/></svg>';
    const iconeOk = '<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M4 12l5 5L20 6"/></svg>';
    function boutonCopier() {
        const b = document.createElement("button");
        b.type = "button";
        b.className = "msg-copier";
        b.title = "Copier la réponse";
        b.setAttribute("aria-label", "Copier la réponse");
        return b;
    }
    function garnirCopier(b) { b.innerHTML = iconeCopier; }
    fil.querySelectorAll(".msg-copier").forEach(garnirCopier);
    fil.addEventListener("click", async (e) => {
        const b = e.target.closest(".msg-copier");
        if (!b) return;
        const prec = b.previousElementSibling;
        const texte = prec?.dataset.brut ?? prec?.textContent ?? "";
        try { await navigator.clipboard.writeText(texte); }
        catch {
            const t = document.createElement("textarea");
            t.value = texte; document.body.appendChild(t); t.select();
            document.execCommand("copy"); t.remove();
        }
        b.innerHTML = iconeOk; b.classList.add("ok");
        setTimeout(() => { b.innerHTML = iconeCopier; b.classList.remove("ok"); }, 1500);
    });
    function rendMarkdown(el) {
        const brut = el.textContent;
        el.dataset.brut = brut;
        if (window.marked && window.DOMPurify) {
            el.innerHTML = DOMPurify.sanitize(marked.parse(brut));
            el.querySelectorAll("a").forEach((a) => { a.target = "_blank"; a.rel = "noopener noreferrer"; });
            el.classList.add("md");
        }
    }
    fil.querySelectorAll(".msg-ia").forEach(rendMarkdown);
    fil.scrollTop = fil.scrollHeight;

    function bulle(role, texte) {
        const d = document.createElement("div");
        d.className = "msg msg-" + role;
        d.textContent = texte;
        if (role === "ia") {
            const g = document.createElement("div");
            g.className = "msg-groupe";
            const bc = boutonCopier(); garnirCopier(bc);
            g.append(d, bc);
            fil.appendChild(g);
        } else fil.appendChild(d);
        fil.scrollTo({ top: fil.scrollHeight, behavior: reduit ? "auto" : "smooth" });
        return d;
    }
    function ajouteHistorique(titre) {
        const a = document.createElement("div");
        a.className = "app-conv nouvelle";
        const lien = document.createElement("span");
        lien.className = "app-conv-lien";
        lien.textContent = titre.length > 40 ? titre.slice(0, 40) + "…" : titre;
        a.appendChild(lien);
        const groupe = $("groupe-aujourdhui");
        if (groupe) groupe.after(a); else historique.prepend(a);
        historique.querySelector(".app-vide")?.remove();
    }

    const fournisseur = $("fournisseur");
    function verrouilleFournisseur(verrou) {
        fournisseur.classList.toggle("verrouille", verrou);
        fournisseur.title = verrou ? "Le fournisseur est fixé pour cette conversation" : "";
    }

    let envoiEnCours = false;
    $("formulaire").addEventListener("submit", (e) => {
        const texte = saisie.value.trim();
        if (!texte || envoiEnCours) { e.preventDefault(); return; }
        envoiEnCours = true;
        app.classList.add("en-cours");
        bulle("moi", texte);
        if (!titreAjoute) { ajouteHistorique(texte); titreAjoute = true; }
        fil.querySelector(".msg-erreur")?.remove();
        saisie.readOnly = true;
        envoyer.disabled = true;
        fournisseur.classList.add("verrouille");
        fil.appendChild($("modele-frappe").content.cloneNode(true));
        fil.scrollTo({ top: fil.scrollHeight, behavior: "smooth" });
        e.preventDefault();
        const form = e.target;
        requestAnimationFrame(() => setTimeout(() => form.submit(), 60));
    });

    historique.addEventListener("submit", (e) => {
        const msg = e.target.dataset.confirme;
        if (msg && !confirm(msg)) e.preventDefault();
    });

    $("btn-nouvelle").addEventListener("click", (e) => {
        if (app.dataset.conv) return;
        e.preventDefault();
        app.classList.remove("en-cours");
        verrouilleFournisseur(false);
        fil.replaceChildren();
        titreAjoute = false;
        saisie.value = "";
        ajuste();
        saisie.focus();
    });
})();
