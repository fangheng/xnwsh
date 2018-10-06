import {
  Component,
  OnInit,
  AfterViewInit,
  HostListener,
  ViewChild,
  ElementRef,
} from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { NzMessageService } from 'ng-zorro-antd';
import { copy, LazyService } from '@delon/util';

import { I18NService, LangType } from '../../core/i18n/service';
import { MobileService } from '../../core/mobile.service';
import { MetaService } from '../../core/meta.service';
import { MetaSearchGroup, MetaSearchGroupItem } from '../../interfaces';

declare const docsearch: any;
declare const algoliasearch: any;

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  host: {
    '[attr.id]': '"header"',
  },
})
export class HeaderComponent implements OnInit, AfterViewInit {
  isMobile: boolean;
  useDocsearch = false;
  oldVersionList = [
    `1.x`
  ];
  currentVersion = 'next';

  constructor(
    public i18n: I18NService,
    private router: Router,
    private msg: NzMessageService,
    private mobileSrv: MobileService,
    private meta: MetaService,
    private lazy: LazyService,
  ) {
    router.events
      .pipe(filter(evt => evt instanceof NavigationEnd))
      .subscribe(() => this.hideMenu());
    this.mobileSrv.change.subscribe(res => (this.isMobile = res));
  }

  ngOnInit(): void {
    if (!this.useDocsearch) this.changeQ('');
  }

  ngAfterViewInit() {
    this.loadDocsearch();
  }

  toVersion(version: string) {
    if (version !== this.currentVersion) {
      window.location.href = `https://ng-alain.github.io/${version}-doc/`;
    }
  }

  private loadDocsearch() {
    if (!this.useDocsearch) return;
    this.lazy
      .load([
        `https://cdn.jsdelivr.net/npm/docsearch.js@2/dist/cdn/docsearch.min.css`,
        `https://cdn.jsdelivr.net/npm/docsearch.js@2/dist/cdn/docsearch.min.js`,
        `https://cdn.jsdelivr.net/algoliasearch/3/algoliasearchLite.min.js`,
      ])
      .then(() => this.initDocSearch());
  }

  @ViewChild('searchInput')
  searchInput: ElementRef<HTMLInputElement>;

  @HostListener('document:keyup.s', ['$event'])
  onKeyUp(event: KeyboardEvent) {
    if (
      this.useDocsearch &&
      this.searchInput &&
      this.searchInput.nativeElement &&
      event.target === document.body
    ) {
      this.searchInput.nativeElement.focus();
    }
  }

  private initDocSearch() {
    docsearch({
      appId: '2WSH9IUML3',
      apiKey: '6356fe022dba23c6bfc63427b2042bf8',
      indexName: 'ng-alain',
      inputSelector: '#search-box input',
      algoliaOptions: {
        hitsPerPage: 5,
        facetFilters: [`tags:${this.i18n.zone}`],
      },
      transformData(hits) {
        hits.forEach(hit => {
          debugger;
          hit.url = hit.url.replace('ng.ant.design', location.host);
          hit.url = hit.url.replace('https:', location.protocol);
        });
        return hits;
      },
      debug: false,
    });
  }

  langChange(language: LangType) {
    this.router.navigateByUrl(
      this.i18n.getRealUrl(this.router.url) + '/' + language,
    );
  }

  onCopy(value: string) {
    copy(value).then(() =>
      this.msg.success(this.i18n.fanyi('app.demo.copied')),
    );
  }

  menuVisible = false;

  showMenu() {
    this.menuVisible = true;
  }

  hideMenu() {
    this.menuVisible = false;
  }

  q: string;
  list: MetaSearchGroup[] = [];
  changeQ(value: any) {
    this.list = this.meta.search(value);
  }

  to(item: MetaSearchGroupItem) {
    if (item.url) {
      this.router.navigateByUrl(item.url);
    }
  }
}
