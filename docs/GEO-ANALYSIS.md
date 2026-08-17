# GEO / AI-Search Analysis — donutsmp-auction-resell-mods (README)

Audit per `seo-geo` skill (May 2026 revision). **Framing:** Google's official
position — *"optimizing for generative AI search is optimizing for the search
experience, and thus still SEO"* (Google Search Central, AI optimization guide,
2026-05-15). Findings are therefore reported as **SEO fundamentals applied to
AI-search surfaces**, not a separate discipline. Where community tactics
contradict Google's primary source, defer to Google and note it here.

## GEO Readiness Score: 84/100 (Good — citable with minor improvements)

| Category | Raw | Display | Max |
|----------|-----|---------|-----|
| Passage-Level Citability | 3.5/4 | 24 | 27 |
| Q&A Formatting | 2.5/3 | 17 | 20 |
| Entity Clarity | 3/3 | 20 | 20 |
| Content Structure | 3/3 | 20 | 20 |
| Technical Accessibility | 1.5/2 | 10 | 13 |
| **Total** | — | **84** | **100** |
| Google AI Overviews (classic pool) | — | Medium | — |
| Google AI Mode (broader pool) | — | Medium-High | — |
| ChatGPT web search | — | Medium-High | — |
| Perplexity | — | Medium | — |

*(AI Overviews and AI Mode are scored separately — per Ahrefs, they reach the
same conclusion ~86% of the time but cite the same URLs only 13.7% of the
time.)*

## What was revised this commit (skill May 2026 update)

1. **Google-first framing** — added a quoted, sourced statement from [Google's
   AI optimization guide](https://developers.google.com/search/docs/fundamentals/ai-optimization-guide)
   ("…still SEO") as a cited claim: primary-source attribution + authoritative.
   Removed any "AI-search hacks" framing.
2. **Front-loading** — ~44% of AI citations come from the first 30% of a page
   (SE Ranking); the "What is this mod?" definition, TL;DR and feature answer
   all sit above the fold, in the first ~25% of the file.
3. **Passage length 134–167 words** — every H2 section rewritten to that
   self-contained range (previously 120–180).
4. **Multi-modal content (+156% selection)** — added a Mermaid pricing-workflow
   diagram (rendered by GitHub) + linked the original author's YouTube video +
   badges (Maintained, Last Updated, MC/Fabric/Java/License).
5. **Recency / refresh program** — new "Maintenance & freshness" section; last
   Updated badge + explicit date; content-under-3-months rationale cited. This
   is the highest-leverage GEO play per the skill (6+ months stale loses
   citation eligibility).
6. **Authority & brand** — author/maintainer + credits with linked credentials
   (fork of ASell by nguyenttuca; original repo + video). Brand-mention
   correlation (> backlinks) noted.
7. **llms.txt correction (myth-busted)** — llms.txt remains **present for
   non-Google AI services**, but this audit no longer claims it as a citation
   lever. Google states it ignores llms.txt and that it "will neither harm nor
   help" visibility/rankings (Search Central, 2026-05-15). Verified by
   SE Ranking (300k-domain study: no measurable ChatGPT citation impact) and
   OtterlyAI (0.1% of AI-crawler requests touch /llms.txt).
8. **No chunking / no AI-rewriting / no mention-farming** — explicitly not
   recommended; not implemented (Google: unnecessary).

## AI Crawler Access Status

- GitHub serves the README as static HTML — no JavaScript execution needed;
  AI crawlers read it directly (GPTBot, OAI-SearchBot, ClaudeBot,
  PerplexityBot all fine).
- robots.txt is GitHub-owned (not editable here).
- llms.txt: **present** (root, `llms.txt`) — kept only for non-Google crawlers
  that honor it; neutral for Google.

## Brand Mention Analysis

- Maintainer: dinhkarate (GitHub entity).
- Original author nguyenttuca + original repo + YouTube video linked (brand
  entity signal, strongest correlated signal per Ahrefs: YouTube mentions
  ~0.737).
- Wikipedia/Reddit/LinkedIn: not applicable for a repository.

## Passage-Level Citability (134–167 word blocks)

- "What is this mod?" — ✓ self-contained definition, answers within first 60
  words of the section.
- "How does the anti price-crash logic work?" — ✓ numbered 4-step + mermaid.
- "How do I resell any enchanted item automatically?" — ✓ numbered 4-step.
- "How does auto-refill from orders work?" — ✓ numbered 5-step.
- "What does the profit tracker report?" — ✓ with concrete example numbers.
- "What anti-detection protections are built in?" — ✓ numbered 6-item.
- "How do I build and install it?" — ✓ command block + requirements.
- FAQ — ✓ 6 Q&A pairs, answer-first.

## SSR / Technical Check

- Static Markdown → GitHub renders server-side; no client-side JS dependency.

## Top 5 highest-impact changes (done)

1. Definition + citable answer from the first 60 words / first 30% of page.
2. Question-based H2s with 134–167-word self-contained passages.
3. Multi-modal: Mermaid diagram + video link + badges.
4. Recency: Last-Updated badge + "Maintenance & freshness" + active-refresh note.
5. llms.txt re-framed (present but neutral for Google) — myth applied.

## Recommendations (next)

- [ ] Create a **release/tag (v1.0.0)** with dated notes — versioned, fresh artifact signal.
- [ ] Add `docs/guides/` deep-dive pages (per-topic extractable surfaces).
- [ ] Add a GitHub Actions CI badge (build passing = activity freshness signal).
- [ ] Encourage community signals (issues/discussions) for Perplexity/community validation.
- [ ] Schedule a 90-day README freshness review (recency is a top GEO lever).